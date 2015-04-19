package org.neo4j.cineasts.repository;

import java.util.List;

import org.neo4j.cineasts.domain.Movie;
import org.neo4j.cineasts.domain.MovieRecommendation;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * @author mh
 * @since 02.04.11
 */
public interface MovieRepository extends GraphRepository<Movie> {
    Movie findById(String id);

    // Page<Movie> findByTitleLike(String title, Pageable page);

    @Query("MATCH (movie:Movie) WHERE movie.title=~{0} RETURN movie")
    Iterable<Movie> findByTitleLike(String title);

    @Query( "match (user:User {login: {0}})-[r:RATED]->(movie)<-[r2:RATED]-(other)-[r3:RATED]->(otherMovie) " +
                    " where r.stars >= 3 and r2.stars >= r.stars and r3.stars >= r.stars " +
                    " and not((user)-[:RATED]->(otherMovie)) " +
                    " with otherMovie, toInt(round(avg(r3.stars))) as rating, count(*) as cnt" +
                    " order by rating desc, cnt desc" +
                    " return otherMovie.id as movieId, otherMovie.title as title, otherMovie.tagline as tagline, rating as rating limit 10" )
    List<MovieRecommendation> getRecommendations(String login);


}
