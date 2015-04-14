package org.neo4j.cineasts.domain;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.cineasts.PersistenceContext;
import org.neo4j.cineasts.repository.ActorRepository;
import org.neo4j.cineasts.repository.DirectorRepository;
import org.neo4j.cineasts.repository.MovieRepository;
import org.neo4j.cineasts.repository.UserRepository;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = {PersistenceContext.class})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DomainTest {

    @Autowired
    ActorRepository actorRepository;
    @Autowired
    DirectorRepository directorRepository;
    @Autowired
    MovieRepository movieRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    Session session;
    
    @Test
    public void shouldAllowActorCreation() {
        Actor tomHanks = new Actor("1", "Tom Hanks");
        tomHanks = actorRepository.save(tomHanks);

        Actor foundTomHanks = findActorByProperty("name", tomHanks.getName()).iterator().next();
        assertEquals(tomHanks.getName(), foundTomHanks.getName());
        assertEquals(tomHanks.getId(), foundTomHanks.getId());

    }

    @Test
    public void shouldAllowDirectorCreation() {
        Director robert = new Director("1", "Robert Zemeckis");
        robert = directorRepository.save(robert);

        Director foundRobert = findDirectorByProperty("name", robert.getName()).iterator().next();
        assertEquals(robert.getId(), foundRobert.getId());
        assertEquals(robert.getName(), foundRobert.getName());

    }

    @Test
    public void shouldAllowMovieCreation() {
        Movie forrest = new Movie("1", "Forrest Gump");
        forrest = movieRepository.save(forrest);

        Movie foundForrest = findMovieByProperty("title", forrest.getTitle()).iterator().next();
        assertEquals(forrest.getId(), foundForrest.getId());
        assertEquals(forrest.getTitle(), foundForrest.getTitle());
    }

    @Test
    public void shouldAllowDirectorToDirectMovie() {
        Movie forrest = new Movie("1", "Forrest Gump");
        forrest = movieRepository.save(forrest);

        Director robert = new Director("1", "Robert Zemeckis");
        robert.directed(forrest);
        robert = directorRepository.save(robert);

        Director foundRobert = findDirectorByProperty("name", robert.getName()).iterator().next();
        assertEquals(robert.getId(), foundRobert.getId());
        assertEquals(robert.getName(), foundRobert.getName());
        assertEquals(forrest, robert.getDirectedMovies().iterator().next());

        Movie foundForrest = findMovieByProperty("title", forrest.getTitle()).iterator().next();
        assertEquals(1, foundForrest.getDirectors().size());
        assertEquals(foundRobert, foundForrest.getDirectors().iterator().next());

    }

    @Test
    public void shouldAllowActorToActInMovie() {
        Movie forrest = new Movie("1", "Forrest Gump");
        forrest = movieRepository.save(forrest);

        Actor tomHanks = new Actor("1", "Tom Hanks");
        tomHanks = actorRepository.save(tomHanks);

        tomHanks.playedIn(forrest, "Forrest Gump");
        tomHanks = actorRepository.save(tomHanks);

        Actor foundTomHanks = findActorByProperty("name", tomHanks.getName()).iterator().next();
        assertEquals(tomHanks.getName(), foundTomHanks.getName());
        assertEquals(tomHanks.getId(), foundTomHanks.getId());
        assertEquals("Forrest Gump", foundTomHanks.getRoles().iterator().next().getName());
    }

    @Test
    public void userCanRateMovie() {
        Movie forrest = new Movie("1", "Forrest Gump");

        User micha = new User("micha", "Micha", "password");
        micha = userRepository.save(micha);

        Rating awesome = micha.rate(forrest, 5, "Awesome");
        userRepository.save(micha);

        User foundMicha = findUserByProperty("login", "micha").iterator().next();
        assertEquals(1, foundMicha.getRatings().size());

        Movie foundForrest = findMovieByProperty("title", forrest.getTitle()).iterator().next();
        assertEquals(1, foundForrest.getRatings().size());

        Rating rating = foundForrest.getRatings().iterator().next();
        assertEquals(awesome, rating);
        assertEquals("Awesome", rating.getComment());
        assertEquals(5, rating.getStars());
        assertEquals(5, foundForrest.getStars(), 0);
    }


    @Test
    public void movieCanBeRatedByUser() {
        Movie forrest = new Movie("1", "Forrest Gump");

        User micha = new User("micha", "Micha", "password");

        Rating awesome = new Rating(micha, forrest, 5, "Awesome");

        micha.getRatings().add(awesome);
        forrest.addRating(awesome);
        movieRepository.save(forrest);

        User foundMicha = findUserByProperty("login", "micha").iterator().next();
        assertEquals(1, foundMicha.getRatings().size());

        Movie foundForrest = findMovieByProperty("id", "1").iterator().next();
        assertEquals(1, foundForrest.getRatings().size());

        Rating rating = foundForrest.getRatings().iterator().next();
        assertEquals(awesome, rating);
        assertEquals("Awesome", rating.getComment());
        assertEquals(5, rating.getStars());
        assertEquals(5, foundForrest.getStars(), 0);
    }

    @Test
    public void testBefriendUsers() {
        final User me = userRepository.register("me", "me", "me");
        final User you = userRepository.save(new User("you", "you", "you"));
        userRepository.addFriend("you", userRepository.getUserFromSession());
        final User loaded = findUserByProperty("login", "me").iterator().next();
        assertEquals(1, loaded.getFriends().size());
    }

    @Test
    public void shouldBeAbleToSaveUserWithSecurityRoles() {
        User micha = new User("micha", "Micha", "password", User.SecurityRole.ROLE_ADMIN, User.SecurityRole.ROLE_USER);
        userRepository.save(micha);

        User foundMicha = findUserByProperty("login","micha").iterator().next();
        assertEquals(micha.getName(),foundMicha.getName());
    }

    @Test
    public void ratingForAMovieByAUserCanBeRetrieved() {
        Movie forrest = new Movie("1", "Forrest Gump");

        User micha = new User("micha", "Micha", "password");
        micha = userRepository.save(micha);

        Rating awesome = micha.rate(forrest, 5, "Awesome");
        micha = userRepository.save(micha);

        Movie foundForrest = findMovieByProperty("id", "1").iterator().next();
        Rating foundAwesome = userRepository.findUsersRatingForMovie(foundForrest.nodeId, micha.nodeId);
        assertNotNull(foundAwesome);
        assertEquals(foundAwesome, awesome);
    }

    @Test
    public void shouldBeAbleToSaveMovieWithTwoDirectors() {
        Movie matrix = new Movie("3", "The Matrix");
        matrix = movieRepository.save(matrix);

        Director andy = new Director("1", "Andy Wachowski");
        andy.directed(matrix);
        directorRepository.save(andy);

        Director lana = new Director("2", "Lana Wachowski");
        lana.directed(matrix);
        directorRepository.save(lana);

        Movie foundMatrix = findMovieByProperty("id", "3").iterator().next();
        assertEquals(2, foundMatrix.getDirectors().size());
    }

    @Test
    public void shouldBeAbleToSaveMovieWithManyActors() {
        Movie matrix = new Movie("3", "The Matrix");

        Actor keanu = new Actor("6384","Keanu Reeves");
        keanu.playedIn(matrix,"Neo");
        actorRepository.save(keanu);

        Actor laurence = new Actor("2975","Laurence Fishburne");
        laurence.playedIn(matrix, "Morpheus");
        actorRepository.save(laurence);

        Actor carrie = new Actor("530", "Carrie-Ann Moss");
        carrie.playedIn(matrix, "Trinity");
        actorRepository.save(carrie);

        Actor foundKeanu = findActorByProperty("id","6384").iterator().next();
        assertEquals(1, foundKeanu.getRoles().size());

        Movie foundMatrix = findMovieByProperty("id", "3").iterator().next();
        assertEquals(3, foundMatrix.getRoles().size());

    }

    @Test
    @Ignore
    public void personShouldBeAbleToBothActInAndDirectMovies() {   //TODO M>1
       /* Movie unforgiven = new Movie("4","Unforgiven");
        unforgiven = movieRepository.save(unforgiven);

        Actor clint = new Actor("5","Clint Eastwood");
        clint = actorRepository.save(clint);
        clint.playedIn(unforgiven,"Bill Munny");
        unforgiven=movieRepository.save(unforgiven);

        Person clintPerson = personRepository.findByProperty("id","5").iterator().next();
        Director clintDirector = new Director(clintPerson);
        clintDirector = directorRepository.save(clintDirector);
        unforgiven.addDirector(clintDirector);
        movieRepository.save(unforgiven);

        Movie foundUnforgiven = findMovieByProperty("id","4").iterator().next();
        assertEquals(1,foundUnforgiven.getDirectors().size());
        assertEquals(1,foundUnforgiven.getRoles().size());
        assertEquals("5",foundUnforgiven.getDirectors().iterator().next().getId());
        assertEquals("5",foundUnforgiven.getRoles().iterator().next().getActor().getId());

        Person p = personRepository.findByProperty("id","5").iterator().next();
        assertNotNull(p);
        Actor actor =  findActorByProperty("id","5").iterator().next();
        assertNotNull(actor);
        Director d = findDirectorByProperty("id","5").iterator().next();
        assertNotNull(d);*/

    }

    @Test
    public void shouldBeAbleToGetEmptyRecommendationsForNewUser() {
        User micha = new User("micha", "Micha", "password", User.SecurityRole.ROLE_ADMIN, User.SecurityRole.ROLE_USER);
        userRepository.save(micha);

        List<Movie> recs = movieRepository.getRecommendations("micha");
        assertEquals(0,recs.size());
    }

    @Test
    public void twoUsersCanRateSameMovie() {
        Movie forrest = new Movie("1", "Forrest Gump");

        User micha = new User("micha", "Micha", "password");
        micha = userRepository.save(micha);

        User luanne = new User("luanne","Luanne","password");
        luanne = userRepository.save(luanne);

        Rating awesome = micha.rate(forrest, 5, "Awesome");
        userRepository.save(micha);


        User foundMicha = findUserByProperty("login", "micha").iterator().next();
        assertEquals(1, foundMicha.getRatings().size());

        Movie foundForrest = findMovieByProperty("title", forrest.getTitle()).iterator().next();
        assertEquals(1, foundForrest.getRatings().size());

        Rating okay = luanne.rate(forrest,3,"Okay");
        userRepository.save(luanne);

        User foundLuanne = findUserByProperty("login", "luanne").iterator().next();
        assertEquals(1, foundLuanne.getRatings().size());
        foundMicha = findUserByProperty("login", "micha").iterator().next();
        assertEquals(1, foundMicha.getRatings().size());

        foundForrest = findMovieByProperty("title", forrest.getTitle()).iterator().next();
        assertEquals(2, foundForrest.getRatings().size());

        Rating rating = foundForrest.getRatings().iterator().next();
        assertEquals(awesome, rating);
        assertEquals("Awesome", rating.getComment());
        assertEquals(5, rating.getStars());
        assertEquals(4, foundForrest.getStars(), 0);
    }

    @Test
    public void shouldLoadActorsForAPersistedMovie() {
        Movie movie = new Movie("600","Die Hard");
        Actor actor = new Actor("Bruce Willis");
        actor.playedIn(movie, "Officer John");
        session.save(actor);

        session.clear();
        Movie dieHard = IteratorUtil.firstOrNull(findMovieByProperty("title","Die Hard"));
        assertNotNull(dieHard);
        assertEquals(1,dieHard.getRoles().size());
    }

    @Test
    public void shouldFindMoviesByTitle() {
        Movie die = new Movie("600","Die Hard");
        Movie matrix = new Movie("601","The Matrix");
        Movie matrixReloaded= new Movie("602","The Matrix Reloaded");
        Movie returnKing= new Movie("603","LOTR The Return of the King");
        session.save(die);
        session.save(matrix);
        session.save(matrixReloaded);
        session.save(returnKing);

        Iterable<Movie> mat = movieRepository.findByTitleLike("(?i).*mat.*");
        List<String> movieIds = new ArrayList<>();
        for(Movie movie : mat) {
            movieIds.add(movie.getId());
        }
        assertEquals(2, movieIds.size());
        assertTrue(movieIds.contains("601"));
        assertTrue(movieIds.contains("602"));

        Movie foundDie = movieRepository.findByTitleLike("(?i).*Die Hard.*").iterator().next();
        assertNotNull(foundDie);
        assertEquals("600", foundDie.getId());

        Iterable<Movie> re = movieRepository.findByTitleLike("(?i).*re.*");
        movieIds = new ArrayList<>();
        for(Movie movie : re) {
            movieIds.add(movie.getId());
        }
        assertEquals(2,movieIds.size());
        assertTrue(movieIds.contains("602"));
        assertTrue(movieIds.contains("603"));
    }

    public Iterable<Actor> findActorByProperty(String propertyName, Object propertyValue) {
        return session.loadByProperty(Actor.class, new Property(propertyName, propertyValue));
    }

    public Iterable<Director> findDirectorByProperty(String propertyName, Object propertyValue) {
        return session.loadByProperty(Director.class, new Property(propertyName, propertyValue));
    }
    public Iterable<Movie> findMovieByProperty(String propertyName, Object propertyValue) {
        return session.loadByProperty(Movie.class, new Property(propertyName, propertyValue));
    }

    public Iterable<Person> findPersonByProperty(String propertyName, Object propertyValue) {
        return session.loadByProperty(Person.class, new Property(propertyName, propertyValue));
    }

    public Iterable<User> findUserByProperty(String propertyName, Object propertyValue) {
        return session.loadByProperty(User.class, new Property(propertyName, propertyValue));
    }
}
