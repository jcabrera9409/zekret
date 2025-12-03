package com.zekret.repository;

import java.util.Optional;

import org.jboss.logging.Logger;

import com.zekret.model.Token;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TokenRepository implements PanacheRepository<Token> {
    private static final Logger LOG = Logger.getLogger(TokenRepository.class);

    /**
     * Find Token by its token string.
     */
    public Optional<Token> findByAccessToken(String accessToken) {
        LOG.debugf("Finding Token by token string: %s", accessToken);
        return find("access_token", accessToken).firstResultOptional();
    }

    /**
     * Invalidate all tokens for a given user by setting loggedOut to true.
     */
    @Transactional
    public void invalidateTokensByUserId(Long userId) {
        LOG.debugf("Invalidating tokens for user ID: %d", userId);
        update("loggedOut = true where user.id = :user_id and loggedOut = false", 
            Parameters.with("user_id", userId));
    }
}