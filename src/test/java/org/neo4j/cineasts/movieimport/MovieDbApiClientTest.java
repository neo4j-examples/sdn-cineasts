package org.neo4j.cineasts.movieimport;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.cineasts.PersistenceContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author mh
 * @since 13.03.11
 */
@ContextConfiguration(classes = {PersistenceContext.class})
@RunWith(SpringRunner.class)
public class MovieDbApiClientTest {

	private static final String API_KEY = "70c7465a780b1d65c0f3d5bd394c5b80";

	@Test
	public void testGetMovie() throws Exception {
		Map movie = new MovieDbApiClient(API_KEY).getMovie("2");
		assertEquals(2, movie.get("id"));
	}

	@Test
	public void testGetPerson() throws Exception {
		Map person = new MovieDbApiClient(API_KEY).getPerson("30112");
		assertEquals(30112, person.get("id"));
	}
}
