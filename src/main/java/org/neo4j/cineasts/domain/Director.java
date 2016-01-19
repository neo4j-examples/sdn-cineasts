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


import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author mh
 * @since 10.11.11
 */
public class Director extends Person {

    @Relationship(type = "DIRECTED")
    private Set<Movie> directedMovies = new HashSet<Movie>();

    public Director(String id, String name) {
        super(id, name);
    }

    public Director() {
    }


    public Director(String id) {
        super(id, null);
    }

    public Set<Movie> getDirectedMovies() {
        return directedMovies;
    }

    public void directed(Movie movie) {
        directedMovies.add(movie);
        movie.addDirector(this);
    }

}
