package com.zekret.repository;

import java.util.Optional;

import org.jboss.logging.Logger;

import com.zekret.model.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    private static final Logger LOG = Logger.getLogger(UserRepository.class);
    
    /**
     * Find User by its email or username.
     */
    public Optional<User> findByEmailOrUsername(String email, String username) {
        LOG.debugf("Finding User by email or username: %s or %s", email, username);
        return find("email = :email or username = :username", 
            Parameters.with("email", email).and("username", username)).firstResultOptional();
    }
}