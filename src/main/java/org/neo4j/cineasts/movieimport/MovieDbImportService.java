/*
 * Copyright [2011-2016] "Neo Technology"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.neo4j.cineasts.movieimport;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.neo4j.cineasts.domain.Actor;
import org.neo4j.cineasts.domain.Director;
import org.neo4j.cineasts.domain.Movie;
import org.neo4j.cineasts.domain.Person;
import org.neo4j.cineasts.domain.Role;
import org.neo4j.cineasts.domain.Roles;
import org.neo4j.cineasts.repository.ActorRepository;
import org.neo4j.cineasts.repository.DirectorRepository;
import org.neo4j.cineasts.repository.MovieRepository;
import org.neo4j.cineasts.repository.PersonRepository;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieDbImportService {

	private static final Logger logger = LoggerFactory.getLogger(MovieDbImportService.class);
	MovieDbJsonMapper movieDbJsonMapper = new MovieDbJsonMapper();

	@Autowired
	private MovieRepository movieRepository;
	@Autowired
	private ActorRepository actorRepository;
	@Autowired
	private DirectorRepository directorRepository;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private Session session;

	private MovieDbApiClient client = new MovieDbApiClient("70c7465a780b1d65c0f3d5bd394c5b80");

	private String baseImageUrl;

	private MovieDbLocalStorage localStorage = new MovieDbLocalStorage("data/json");

	public void importImageConfig() {
		Map data = client.getImageConfig();
		baseImageUrl = ((Map) data.get("images")).get("base_url") + "w185";
	}

	@Transactional
	public Map<Integer, String> importMovies(Map<Integer, Integer> ranges) {
		final Map<Integer, String> movies = new LinkedHashMap<Integer, String>();
		for (Map.Entry<Integer, Integer> entry : ranges.entrySet()) {
			for (int id = entry.getKey(); id <= entry.getValue(); id++) {
				String result = importMovieFailsafe(id);
				movies.put(id, result);
			}
		}
		return movies;
	}

	private String importMovieFailsafe(Integer id) {
		try {
			Movie movie = doImportMovie(String.valueOf(id));
			return movie.getTitle();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public Movie importMovie(String movieId) {
		return doImportMovie(movieId);
	}

	private Movie doImportMovie(String movieId) {
		logger.debug("Importing movie " + movieId);

		Movie movie = movieRepository.findById(movieId);
		if (movie == null) { // Not found: Create fresh
			movie = new Movie(movieId, null);
		}

		Map data = loadMovieData(movieId);
		if (data.containsKey("not_found")) {
			throw new RuntimeException("Data for Movie " + movieId + " not found.");
		}
		movieDbJsonMapper.mapToMovie(data, movie, baseImageUrl);
		movieRepository.save(movie);
		relatePersonsToMovie(movie, (Map) data.get("credits"));
		return movie;
	}

	private Map loadMovieData(String movieId) {
		if (localStorage.hasMovie(movieId)) {
			return localStorage.loadMovie(movieId);
		}

		Map data = client.getMovie(movieId);
		localStorage.storeMovie(movieId, data);
		return data;
	}

	private void relatePersonsToMovie(Movie movie, Map data) {
		//Relate Crew
		@SuppressWarnings("unchecked") Collection<Map> crew = (Collection<Map>) data.get("crew");
		for (Map entry : crew) {
			String id = "" + entry.get("id");
			String jobName = (String) entry.get("job");
			Roles job = movieDbJsonMapper.mapToRole(jobName);
			if (job == null) {
				if (logger.isInfoEnabled()) {
					logger.info("Could not add person with job " + jobName + " " + entry);
				}
				continue;
			}
			if (Roles.DIRECTED.equals(job)) {
				Director director = null;
				director = doImportDirector(id, new Director(id));
				if (director == null) {
					logger.debug("Person " + id + " and job " + jobName + " already exists as an actor");
				}
				if (director != null && !director.getDirectedMovies().contains(movie)) {
					director.directed(movie);
					try {
						directorRepository.save(director, 1);
					} catch (Exception e) {
						throw e;
					}
				}
				//movieRepository.save(movie);

			}
		}

		//Relate Cast
		@SuppressWarnings("unchecked") Collection<Map> cast = (Collection<Map>) data.get("cast");
		for (Map entry : cast) {
			String id = "" + entry.get("id");
			final Actor actor = doImportActor(id, new Actor(id));
			if (actor == null) {
				logger.debug("Person " + id + " and job Actor already exists as an actor");
			}
			if (actor!=null && !actor.getRoles().contains(new Role(actor, movie, (String) entry.get("character")))) {
				actor.playedIn(movie, (String) entry.get("character"));
				try {
					actorRepository.save(actor);
				} catch (Exception e) {
					logger.debug("Person id " + id + " and name " + actor.getName() + " couldnt be saved");
					throw e;
				}
			}
		}
	}

	/*@Transactional
	public <T extends Person> T importPerson(String personId, T person) {
		return doImportPerson(personId, person);
	}*/
/*
	private <T extends Person> T doImportPerson(String personId, T newPerson) {
        logger.debug("Importing person " + personId);
        Person person = IteratorUtil.singleOrNull(findPersonByProperty("id", personId));
        if (person != null) {
            return (T) person;
        }
        Map data = loadPersonData(personId);
        if (data.containsKey("not_found")) {
            throw new RuntimeException("Data for Person " + personId + " not found.");
        }
        movieDbJsonMapper.mapToPerson(data, newPerson,baseImageUrl);
        return personRepository.save(newPerson);
    }*/

	private Actor doImportActor(String personId, Actor newPerson) {
		logger.debug("Importing actor " + personId);
		Actor actor = (actorRepository.findById(personId));
		if (actor != null) {
			return actor;
		}
		//Check if the person is a director
		if (directorRepository.findById(personId) != null) {
			return null;
		}
		Map data = loadPersonData(personId);
		if (data.containsKey("not_found")) {
			throw new RuntimeException("Data for Person " + personId + " not found.");
		}
		movieDbJsonMapper.mapToPerson(data, newPerson, baseImageUrl);
		return actorRepository.save(newPerson);
	}

	private Director doImportDirector(String personId, Director newPerson) {
		logger.debug("Importing director " + personId);
		Director director = directorRepository.findById(personId);
		if (director != null) {
			return director;
		}
		if (actorRepository.findById(personId) != null) {
			return null;
		}
		Map data = loadPersonData(personId);
		if (data.containsKey("not_found")) {
			throw new RuntimeException("Data for Person " + personId + " not found.");
		}
		movieDbJsonMapper.mapToPerson(data, newPerson, baseImageUrl);
		return directorRepository.save(newPerson);
	}

	private Map loadPersonData(String personId) {
		if (localStorage.hasPerson(personId)) {
			return localStorage.loadPerson(personId);
		}
		Map data = client.getPerson(personId);
		localStorage.storePerson(personId, data);
		return localStorage.loadPerson(personId);
	}

	public Iterable<Person> findPersonByProperty(String propertyName, Object propertyValue) {
		return session.loadAll(Person.class, new Filter(propertyName, propertyValue));
	}
}
