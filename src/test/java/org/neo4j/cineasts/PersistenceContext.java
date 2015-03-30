package org.neo4j.cineasts;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.server.InProcessServer;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableNeo4jRepositories("org.neo4j.cineasts.repository")
@EnableTransactionManagement
@ComponentScan("org.neo4j.cineasts")
public class PersistenceContext extends Neo4jConfiguration {

    @Override
    public SessionFactory getSessionFactory() {
        return new SessionFactory("org.neo4j.cineasts.domain");
    }

    @Bean
    public Neo4jServer neo4jServer() {
        return new InProcessServer();
    }

    @Override
    @Bean
    public Session getSession() throws Exception {
        return super.getSession();
    }
}
