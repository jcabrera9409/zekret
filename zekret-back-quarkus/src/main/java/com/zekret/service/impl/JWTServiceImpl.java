package com.zekret.service.impl;

import java.time.Duration;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.zekret.exception.InternalServerException;
import com.zekret.model.User;
import com.zekret.service.IJWTService;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JWTServiceImpl implements IJWTService {
    private static final Logger LOG = Logger.getLogger(JWTServiceImpl.class);

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    private String issuer;

    @ConfigProperty(name = "jwt.expiration.time")
    private long expirationTime;

    @Override
    public String generateToken(User user) {
        try {
            LOG.infof("Generating token for user: %s", user.getEmail());

            return Jwt.issuer(issuer)
                    .upn(user.getEmail())
                    .groups(Set.of("user"))
                    .expiresIn(Duration.ofSeconds(expirationTime))
                    .claim("username", user.getUsername())
                    .sign();
                    
        } catch (JwtSignatureException e) {
            LOG.errorf(e, "Error generating JWT token for user: %s", user.getEmail());
            throw new InternalServerException("Error generating JWT token", e);
        }
    }
    
}