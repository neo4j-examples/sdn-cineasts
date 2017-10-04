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
package org.neo4j.cineasts.repository;

import java.util.List;

import org.neo4j.cineasts.domain.Movie;
import org.neo4j.cineasts.domain.MovieRecommendation;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * @author mh
 * @since 02.04.11
 */
public interface MovieRepository extends Neo4jRepository<Movie, Long> {
	Movie findById(String id);

	// Page<Movie> findByTitleLike(String title, Pageable page);

	@Query("MATCH (movie:Movie) WHERE movie.title=~{0} RETURN movie")
	Iterable<Movie> findByTitleLike(String title);

	@Query("match (user:User {login: {0}})-[r:RATED]->(movie)<-[r2:RATED]-(other)-[r3:RATED]->(otherMovie) "
			+ " where r.stars >= 3 and r2.stars >= r.stars and r3.stars >= r.stars "
			+ " and not((user)-[:RATED]->(otherMovie)) "
			+ " with otherMovie, toInt(round(avg(r3.stars))) as rating, count(*) as cnt"
			+ " order by rating desc, cnt desc"
			+ " return otherMovie.id as movieId, otherMovie.title as title, otherMovie.tagline as tagline, rating as rating limit 10")
	List<MovieRecommendation> getRecommendations(String login);

}
