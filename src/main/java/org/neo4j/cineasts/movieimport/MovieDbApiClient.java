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

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MovieDbApiClient {

    protected final ObjectMapper mapper;
    private final String baseUrl = "https://api.themoviedb.org/3";
    private final String apiKey;

    public MovieDbApiClient(String apiKey) {
        this.apiKey = apiKey;
        mapper = new ObjectMapper();
    }

    public Map getMovie(String id) {
        return loadJsonData(buildMovieUrl(id));
    }

    private Map loadJsonData(String url) {
        try {
            Map value = mapper.readValue(new URL(url), Map.class);
            if (value.isEmpty()) {
                return Collections.singletonMap("not_found", System.currentTimeMillis());
            }
            return value;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data from " + url, e);
        }
    }

    private String buildMovieUrl(String movieId) {
        return String.format("%s/movie/%s?append_to_response=credits&api_key=%s", baseUrl, movieId, apiKey);
    }

    public Map getPerson(String id) {
        return loadJsonData(buildPersonUrl(id));
    }

    private String buildPersonUrl(String personId) {
        return String.format("%s/person/%s?api_key=%s", baseUrl, personId, apiKey);
    }

    public Map getImageConfig() {
        String url = String.format("%s/configuration?api_key=%s",baseUrl,apiKey);
        return loadJsonData(url);
    }
}
