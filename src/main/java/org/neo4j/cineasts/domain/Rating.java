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
package org.neo4j.cineasts.domain;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "RATED")
public class Rating {

	private Long id;
	@StartNode private User user;
	@EndNode private Movie movie;
	private int stars;
	private String comment;

	public Rating() {}

	public Rating(User user, Movie movie, int stars, String comment) {
		this.user = user;
		this.movie = movie;
		this.stars = stars;
		this.comment = comment;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Movie getMovie() {
		return movie;
	}

	public void setMovie(Movie movie) {
		this.movie = movie;
	}

	public int getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Rating)) {
			return false;
		}

		Rating rating = (Rating) o;

		if (stars != rating.stars) {
			return false;
		}
		if (comment != null ? !comment.equals(rating.comment) : rating.comment != null) {
			return false;
		}
		if (movie != null ? !movie.equals(rating.movie) : rating.movie != null) {
			return false;
		}
		if (user != null ? !user.equals(rating.user) : rating.user != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = user != null ? user.hashCode() : 0;
		result = 31 * result + (movie != null ? movie.hashCode() : 0);
		result = 31 * result + stars;
		result = 31 * result + (comment != null ? comment.hashCode() : 0);
		return result;
	}
}
