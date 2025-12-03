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
    public Optional<User> findByEmailOrUsername(String emailOrUsername) {
        LOG.debugf("Finding User by email or username: %s", emailOrUsername);
        return find("email = :emailOrUsername or username = :emailOrUsername", 
            Parameters.with("emailOrUsername", emailOrUsername)).firstResultOptional();
    }
}