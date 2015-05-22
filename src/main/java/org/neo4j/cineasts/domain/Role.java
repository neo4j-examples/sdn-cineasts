package org.neo4j.cineasts.domain;


import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author mh
 * @since 04.03.11
 */
@RelationshipEntity(type="ACTS_IN")
public class Role {

    @GraphId
    Long id;
    @EndNode
    Movie movie;
    @StartNode
    Actor actor;

    String name;

    public Role() {
    }

    public Role(Actor actor, Movie movie, String roleName) {
        this.movie = movie;
        this.actor = actor;
        this.name = roleName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Movie getMovie() {
        return movie;
    }

    public Actor getActor() {
        return actor;
    }

    @Override
    public String toString() {
        return String.format("%s acts as %s in %s", actor, name, movie);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        if (movie != null ? !movie.equals(role.movie) : role.movie != null) return false;
        if (actor != null ? !actor.equals(role.actor) : role.actor != null) return false;
        return !(name != null ? !name.equals(role.name) : role.name != null);
    }

    @Override
    public int hashCode() {
        int result = movie != null ? movie.hashCode() : 0;
        result = 31 * result + (actor != null ? actor.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
