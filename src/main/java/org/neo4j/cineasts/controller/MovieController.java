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
package org.neo4j.cineasts.controller;

import static org.neo4j.ogm.cypher.ComparisonOperator.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.neo4j.cineasts.domain.Actor;
import org.neo4j.cineasts.domain.Movie;
import org.neo4j.cineasts.domain.Rating;
import org.neo4j.cineasts.domain.Role;
import org.neo4j.cineasts.domain.User;
import org.neo4j.cineasts.repository.ActorRepository;
import org.neo4j.cineasts.repository.MovieRepository;
import org.neo4j.cineasts.repository.UserRepository;
import org.neo4j.cineasts.service.DatabasePopulator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author mh
 * @since 04.03.11
 */
@Controller
public class MovieController {

	private static final Logger log = LoggerFactory.getLogger(MovieController.class);
	@Autowired private MovieRepository movieRepository;
	@Autowired private ActorRepository actorRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private DatabasePopulator populator;
	@Autowired private Session session;

	@RequestMapping(value = "/movies/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public @ResponseBody Movie getMovie(@PathVariable String id) {
		final Iterator<Movie> movieIterator = findMovieByProperty("id", id).iterator();
		return movieIterator.hasNext() ? movieIterator.next() : null;
	}

	@RequestMapping(value = "/movies/{movieId}", method = RequestMethod.GET, headers = "Accept=text/html")
	public String singleMovieView(final Model model, @PathVariable String movieId) {
		User user = addUser(model);
		final Iterator<Movie> movieIterator = findMovieByProperty("id", movieId).iterator();
		Movie movie = movieIterator.hasNext() ? movieIterator.next() : null;
		model.addAttribute("id", movieId);
		if (movie != null) {
			model.addAttribute("movie", movie);
			final int stars = movie.getStars();
			model.addAttribute("stars", stars);
			Rating rating = null;
			if (user != null) {
				for (Rating r : user.getRatings()) {
					if (r.getMovie().equals(movie)) {
						rating = r;
						break;
					}
				}
			}
			if (rating == null) {
				rating = new Rating();
				rating.setMovie(movie);
				rating.setUser(user);
				rating.setStars(stars);
			}
			model.addAttribute("userRating", rating);
		}
		return "/movies/show";
	}

	@RequestMapping(value = "/movies/{movieId}", method = RequestMethod.POST, headers = "Accept=text/html")
	public String updateMovie(Model model, @PathVariable String movieId,
			@RequestParam(value = "rated", required = false) Integer stars,
			@RequestParam(value = "comment", required = false) String comment) {
		final Iterator<Movie> movieIterator = findMovieByProperty("id", movieId).iterator();
		Movie movie = movieIterator.hasNext() ? movieIterator.next() : null;
		User user = userRepository.getUserFromSession();
		if (user != null && movie != null) {
			int stars1 = stars == null ? -1 : stars;
			String comment1 = comment != null ? comment.trim() : null;
			user.rate(movie, stars1, comment1);
			userRepository.save(user);
		}
		return singleMovieView(model, movieId);
	}

	private User addUser(Model model) {
		User user = userRepository.getUserFromSession();
		model.addAttribute("user", user);
		return user;
	}

	@RequestMapping(value = "/movies", method = RequestMethod.GET, headers = "Accept=text/html")
	public String findMovies(Model model, @RequestParam("q") String query) {
		if (query != null && !query.isEmpty()) {
			// Page<Movie> movies = movieRepository.findByTitleLike(query, new PageRequest(0, 20));
			final Set<Movie> movies = new HashSet<>();
			for (Movie movie : movieRepository.findByTitleLike("(?i).*" + query + ".*")) {
				movies.add(movie);
			}

			model.addAttribute("movies", movies);
		} else {
			model.addAttribute("movies", Collections.emptyList());
		}
		model.addAttribute("query", query);
		addUser(model);
		return "/movies/list";
	}

	@RequestMapping(value = "/actors/{id}", method = RequestMethod.GET, headers = "Accept=text/html")
	public String singleActorView(Model model, @PathVariable String id) {
		final Iterator<Actor> actorIterator = findActorByProperty("id", id).iterator();
		Actor actor = actorIterator.hasNext() ? actorIterator.next() : null;
		model.addAttribute("actor", actor);
		model.addAttribute("id", id);

		final Set<Role> roles = new HashSet<>();
		for (Role role : actor.getRoles()) {
			roles.add(role);
		}

		model.addAttribute("roles", roles);
		addUser(model);
		return "/actors/show";
	}

	@RequestMapping(value = "/populate", method = RequestMethod.GET)
	public String populateDatabase(Model model) {
		Collection<Movie> movies = populator.populateDatabase();
		model.addAttribute("movies", movies);
		addUser(model);
		return "/movies/list";
	}

	@RequestMapping(value = "/admin/clean", method = RequestMethod.GET)
	public String clean(Model model) {
		populator.cleanDb();
		return "movies/list";
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index(Model model) {
		addUser(model);
		return "index";
	}

	public Iterable<Movie> findMovieByProperty(String propertyName, Object propertyValue) {
		return session.loadAll(Movie.class, new Filter(propertyName, EQUALS, propertyValue));
	}

	public Iterable<Actor> findActorByProperty(String propertyName, Object propertyValue) {
		return session.loadAll(Actor.class, new Filter(propertyName, EQUALS, propertyValue));
	}
}
