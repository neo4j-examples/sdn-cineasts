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

import java.util.Collections;
import java.util.Map;

import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author mh
 * @since 04.10.11
 */
public class MovieImporter {

    private final MovieDbImportService importer;

    public MovieImporter(MovieDbImportService importer) {
        this.importer = importer;
    }

    public static void main(String[] args) {
        final FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext("src/main/webapp/WEB-INF/applicationContext.xml");
        try {
            final MovieDbImportService importer = ctx.getBean(MovieDbImportService.class);
            final MovieImporter movieImporter = new MovieImporter(importer);
            movieImporter.runImport(getMovieIdsToImport(args));
        } finally {
            ctx.close();
        }
    }

    private static Map<Integer, Integer> getMovieIdsToImport(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Usage: MovieImporter 1 10000\nWorking Directory should be the cineasts directory with the json files in data/json.");
        }
        if (args.length == 1) {
            return Collections.singletonMap(Integer.valueOf(args[0]), Integer.valueOf(args[0]));
        }
        return Collections.singletonMap(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
    }

    private void runImport(Map<Integer, Integer> movieIdsToImport) {
        final long start = System.currentTimeMillis();
        final Map<Integer, String> result = importer.importMovies(movieIdsToImport);
        final long time = System.currentTimeMillis() - start;
        for (Map.Entry<Integer, String> movie : result.entrySet()) {
            System.out.println(movie.getKey() + "\t" + movie.getValue());
        }
        System.out.println("Imported movies took " + time + " ms.");
    }
}
