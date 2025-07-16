package com.zekret.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.zekret.dto.AuthenticationResponseDTO;
import com.zekret.model.Token;
import com.zekret.model.User;
import com.zekret.repo.ITokenRepo;
import com.zekret.repo.IUserRepo;

@Service
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Autowired
    private IUserRepo repository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;

    @Autowired
    private ITokenRepo tokenRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Authenticate user and generate JWT tokens
     */
    public AuthenticationResponseDTO authenticate(User request) {
        logger.info("Authenticating user: {}", request.getUsername());
        
        try {
            // Authenticate user credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Find user by email or username
            User usuario = repository.findByEmailOrUsername(request.getUsername(), request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (usuario.isEnabled()) {
                logger.info("User authenticated successfully: {}", usuario.getUsername());
                
                // Generate tokens
                String accessToken = jwtService.generateAccessToken(usuario);
                String refreshToken = jwtService.generateRefreshToken(usuario);

                // Revoke existing tokens and save new ones
                revokeAllTokenByUser(usuario);
                saveUserToken(accessToken, refreshToken, usuario);

                return new AuthenticationResponseDTO(accessToken, refreshToken, "User authenticated successfully");
            } else {
                logger.warn("Account is disabled for user: {}", usuario.getUsername());
                return new AuthenticationResponseDTO(null, null, "Your account is not enabled. Please contact support.");
            }
            
        } catch (Exception e) {
            logger.error("Authentication failed for user: {} - {}", request.getUsername(), e.getMessage());
            return new AuthenticationResponseDTO(null, null, "Invalid credentials");
        }
    }

    /**
     * Revoke all active tokens for a user
     */
    public void revokeAllTokenByUser(User usuario) {
        logger.info("Revoking all tokens for user: {}", usuario.getUsername());
        
        List<Token> validTokens = tokenRepository.findByUserIdAndLoggedOutFalse(usuario.getId());
        if (validTokens.isEmpty()) {
            logger.debug("No active tokens found for user: {}", usuario.getUsername());
            return;
        }

        validTokens.forEach(token -> token.setLoggedOut(true));
        tokenRepository.saveAll(validTokens);
        
        logger.info("Revoked {} tokens for user: {}", validTokens.size(), usuario.getUsername());
    }

    /**
     * Save new token for user
     */
    private void saveUserToken(String accessToken, String refreshToken, User usuario) {
        logger.info("Saving new tokens for user: {}", usuario.getUsername());
        
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(usuario);
        tokenRepository.save(token);
    }

    /**
     * Update user password by ID
     */
    public int updatePasswordById(Long id, String password) {
        logger.info("Updating password for user ID: {}", id);
        
        int rowsUpdated = repository.updatePasswordById(id, passwordEncoder.encode(password));
        
        logger.info("Password updated for user ID: {} - {} rows affected", id, rowsUpdated);
        return rowsUpdated;
    }
}
