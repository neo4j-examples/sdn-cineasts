package org.neo4j.cineasts.domain;

import org.springframework.data.neo4j.annotation.QueryResult;

/**
 * @author mh
 * @since 04.11.11
 */
@QueryResult
public class MovieRecommendation {

    Movie movie;
    String movieId;
    String title;
    String tagline;

    int rating;

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }
}
