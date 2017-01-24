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
package org.neo4j.cineasts.integration;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.cineasts.PersistenceContext;
import org.neo4j.cineasts.domain.Actor;
import org.neo4j.cineasts.domain.Director;
import org.neo4j.cineasts.domain.Movie;
import org.neo4j.cineasts.repository.ActorRepository;
import org.neo4j.cineasts.repository.DirectorRepository;
import org.neo4j.cineasts.repository.MovieRepository;
import org.neo4j.cineasts.service.Neo4jDatabaseCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(classes = {PersistenceContext.class})
@RunWith(SpringRunner.class)
public class DatabasePopulationTest {

	@Autowired
	ActorRepository actorRepository;

	@Autowired
	DirectorRepository directorRepository;

	@Autowired
	MovieRepository movieRepository;

	@Autowired
	Neo4jDatabaseCleaner cleaner;

	@Test
	@Transactional
	public void databaseShouldBeCleared() {

		Actor tomHanks = new Actor("1", "Tom Hanks");
		actorRepository.save(tomHanks);

		Movie forrest = new Movie("1", "Forrest Gump");
		forrest = movieRepository.save(forrest);

		Director robert = new Director("1", "Robert Zemeckis");
		robert.directed(forrest);
		directorRepository.save(robert);

		cleaner.cleanDb();
	}
}
