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
package org.neo4j.cineasts.repository;

import org.neo4j.cineasts.domain.User;
import org.neo4j.cineasts.service.CineastsUserDetails;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mh
 * @since 06.03.11
 */
public class
        UserRepositoryImpl implements CineastsUserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Session session;

    @Override
    public CineastsUserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        final User user = findByLogin(login);
        if (user == null) {
            throw new UsernameNotFoundException("Username not found: " + login);
        }
        return new CineastsUserDetails(user);
    }

    private User findByLogin(String login) {
        return IteratorUtil.firstOrNull(findByProperty("login", login).iterator());
    }

    @Override
    public User getUserFromSession() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof CineastsUserDetails) {
            CineastsUserDetails userDetails = (CineastsUserDetails) principal;
            return userDetails.getUser();
        }
        return null;
    }

    @Override
    @Transactional
    public User register(String login, String name, String password) {
        User found = findByLogin(login);
        if (found != null) {
            throw new RuntimeException("Login already taken: " + login);
        }
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("No name provided.");
        }
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("No password provided.");
        }
        User user=userRepository.save(new User(login,name,password, User.SecurityRole.ROLE_USER));
        setUserInSession(user);
        return user;
    }

    void setUserInSession(User user) {
        SecurityContext context = SecurityContextHolder.getContext();
        CineastsUserDetails userDetails = new CineastsUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, user.getPassword(), userDetails.getAuthorities());
        context.setAuthentication(authentication);

    }

    @Override
    @Transactional
    public void addFriend(String friendLogin, final User user) {
        User friend = findByLogin(friendLogin);
        if (!user.equals(friend)) {
            user.addFriend(friend);
            userRepository.save(user);
        }
    }

    public Iterable<User> findByProperty(String propertyName, Object propertyValue) {
        return session.loadAll(User.class, new Filter(propertyName, propertyValue));
    }

}
