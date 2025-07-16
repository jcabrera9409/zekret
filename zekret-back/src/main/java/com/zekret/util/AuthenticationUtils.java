package com.zekret.util;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zekret.model.User;
import com.zekret.service.IUserService;
import com.zekret.service.impl.JwtService;

/**
 * Utility class for JWT authentication operations
 * Provides common methods for extracting and validating users from JWT tokens
 */
@Component
public class AuthenticationUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationUtils.class);
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private IUserService userService;
    
    /**
     * Extracts and validates the authenticated user from the JWT token in the Authorization header
     * 
     * @param authorizationHeader The Authorization header containing the Bearer token
     * @return The authenticated User entity
     * @throws RuntimeException if the header is invalid or user is not found
     */
    public User getAuthenticatedUserFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.error("Invalid or missing Authorization header");
            throw new RuntimeException("Invalid or missing Authorization header");
        }
        
        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
        String username = jwtService.extractUsername(token);
        logger.info("Getting authenticated user: {}", username);
        
        // Search in all users
        List<User> allUsers = userService.getAll();
        Optional<User> userOpt = allUsers.stream()
                .filter(user -> user.getUsername().equals(username) || user.getEmail().equals(username))
                .findFirst();
        
        if (userOpt.isPresent()) {
            logger.debug("User found and authenticated: {}", username);
            return userOpt.get();
        } else {
            logger.error("User not found: {}", username);
            throw new RuntimeException("User not found: " + username);
        }
    }
    
    /**
     * Validates that the Authorization header has the correct Bearer format
     * 
     * @param authorizationHeader The Authorization header to validate
     * @return true if the header is valid, false otherwise
     */
    public boolean isValidAuthorizationHeader(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }
    
    /**
     * Extracts the token from the Authorization header
     * 
     * @param authorizationHeader The Authorization header containing the Bearer token
     * @return The JWT token without the "Bearer " prefix
     * @throws RuntimeException if the header is invalid
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (!isValidAuthorizationHeader(authorizationHeader)) {
            throw new RuntimeException("Invalid Authorization header format");
        }
        return authorizationHeader.substring(7);
    }
}
