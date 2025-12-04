package com.zekret.configuration;

import java.io.IOException;
import java.time.Instant;

import org.jboss.logging.Logger;

import com.zekret.model.Token;
import com.zekret.repository.TokenRepository;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class JWTAuthFilter implements ContainerRequestFilter {
    private static final Logger LOG = Logger.getLogger(JWTAuthFilter.class);

    private final TokenRepository tokenRepository;
    private final SecurityIdentity securityIdentity;
    private final JWTParser jwtParser;

    public JWTAuthFilter(TokenRepository tokenRepository, SecurityIdentity securityIdentity, JWTParser jwtParser) {
        this.tokenRepository = tokenRepository;
        this.securityIdentity = securityIdentity;
        this.jwtParser = jwtParser;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.info("JWTAuthFilter invoked for request: " + requestContext.getUriInfo().getRequestUri().toString());
        
        if (securityIdentity.isAnonymous()) {
            LOG.info("Request is anonymous, skipping JWT validation.");
            return;
        }

        LOG.info("Validating JWT token for user: " + securityIdentity.getPrincipal().getName());
        String authHeader = requestContext.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOG.warn("Missing or invalid Authorization header.");
            throw new IOException("Missing or invalid Authorization header.");
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        Token storedToken = tokenRepository.findByAccessToken(token)
                                .orElseThrow(() -> {
                                    LOG.warn("Token not found in repository.");
                                    return new IOException("Invalid token.");
                                });

        if(storedToken.isLoggedOut()) {
            LOG.warn("Invalid or logged out token.");
            throw new IOException("Invalid or logged out token.");
        }

        if (isExpired(token)) {
            LOG.warn("Token has expired.");
            throw new IOException("Token has expired.");
        }
    }

    /**
     * Check if the token is expired.
     */
    public boolean isExpired(String token) {
        Instant exp = getExpiration(token);
        return exp != null && Instant.now().isAfter(exp);
    }

    /**
     * Get the expiration time of the token.
     */
    public Instant getExpiration(String token) {
        JWTCallerPrincipal principal = parseToken(token);
        Object exp = principal.getClaim("exp");
        if (exp instanceof Long aLong) {
            return Instant.ofEpochSecond(aLong);
        }
        return null;
    }

    /**
     * Parse a token and return the principal.
     */
    private JWTCallerPrincipal parseToken(String token) {
        LOG.info("Parsing token in JWTAuthFilter.");
        try {
            return (JWTCallerPrincipal) jwtParser.parse(token);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

}