package org.neo4j.cineasts.movieimport;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.neo4j.ogm.cypher.ComparisonOperator.EQUALS;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.cineasts.PersistenceContext;
import org.neo4j.cineasts.domain.Movie;
import org.neo4j.cineasts.repository.MovieRepository;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mh
 * @since 13.03.11
 */
@ContextConfiguration(classes = {PersistenceContext.class})
@RunWith(SpringRunner.class)
@Transactional
public class MovieDbImportServiceTest {

	@Autowired
	MovieDbImportService importService;
	@Autowired
	MovieRepository movieRepository;

	@Autowired
	Session session;

	@Test
	public void testImportMovie() throws Exception {
		Movie movie = importService.importMovie("2");
		assertEquals("movie-id", "2", movie.getId());
		assertEquals("movie-title", "Ariel", movie.getTitle());
	}

	@Test
	@Ignore
	public void testImportMovieWithSamePersonAsActorAndDirector() throws Exception {
		Movie movie = importService.importMovie("200");
		assertEquals("movie-id", "200", movie.getId());
		assertEquals("movie-title", "Star Trek: Insurrection", movie.getTitle());
	}

	@Test
	public void testImportMovieTwice() throws Exception {
		Movie movie = importService.importMovie("603");
		importService.importMovie("603");
		final Movie foundMovie = movieRepository.findById("603");
		assertEquals("movie-id", movie, foundMovie);
	}

	/*@Test
	public void testImportPerson() throws Exception {
		Person actor = importService.importPerson("105955", new Actor("105955", null));
		assertEquals("person-id", "105955", actor.getId());
		assertEquals("person-title", "George M. Williamson", actor.getName());
	}*/

	@Test
	public void shouldImportMovieWithTwoDirectors() throws Exception {
		importService.importMovie("603");
		Movie movie = findMovieByProperty("id", "603").iterator().next();
		assertEquals(2, movie.getDirectors().size());
	}

	@Test
	@Ignore
	public void testMultipleImports() throws Exception {
		List<Integer> ids = asList(19995, 194, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609, 13, 20526, 11, 1893, 1892, 1894, 168, 193, 200, 157, 152, 201, 154, 12155, 58, 285, 118, 22, 392, 5255, 568, 9800, 497, 101, 120, 121, 122);

		//	List<Integer> ids= Arrays.asList(603, 604, 605); //just 603,604 for actors
		for (Integer id : ids) {
			importService.importMovie(Integer.toString(id));
		}
	}

	public Iterable<Movie> findMovieByProperty(String propertyName, Object propertyValue) {
		return session.loadAll(Movie.class, new Filter(propertyName, EQUALS, propertyValue));
	}

	/*@Test
	public void testActorWith2Roles() throws Exception {
		Movie m1 = new Movie("1", "Matrix");
		session.save(m1);

		Movie m2 = new Movie("2", "Matrix reloaded");
		session.save(m2);

		Actor carrie = new Actor("100", "Carrie");
		carrie.playedIn(m1, "role1");
		session.save(carrie);

		carrie.playedIn(m2, "role2");
		session.save(carrie);
	}

	@Test
	public void testDirWith2Roles() throws Exception {
		Movie m1 = new Movie("1", "Matrix");
		session.save(m1);

		Director andy = new Director("1000", "Andy");
		andy.directed(m1);
		session.save(andy);

		Movie m2 = new Movie("2", "Matrix reloaded");
		session.save(m2);
		andy = session.load(Director.class, andy.getNodeId());
		andy.directed(m2);
		session.save(andy);

		Movie m3 = new Movie("3", "Matrix again");
		session.save(m3);
		andy = session.load(Director.class, andy.getNodeId());
		andy.directed(m3);
		session.save(andy);
	}*/
}
