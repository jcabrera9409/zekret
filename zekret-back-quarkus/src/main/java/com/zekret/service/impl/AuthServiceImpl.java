package com.zekret.service.impl;

import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

import com.zekret.dto.AuthResponseDTO;
import com.zekret.exception.ResourceNotFoundException;
import com.zekret.exception.UnauthorizedException;
import com.zekret.model.Token;
import com.zekret.model.User;
import com.zekret.repository.TokenRepository;
import com.zekret.repository.UserRepository;
import com.zekret.service.IAuthService;
import com.zekret.service.IJWTService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthServiceImpl implements IAuthService {
    private static final Logger LOG = Logger.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;

    private final IJWTService jwtService;

    public AuthServiceImpl(UserRepository userRepository, TokenRepository tokenRepository, IJWTService jwtService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponseDTO authenticate(String username, String password) {
        LOG.infof("Authenticating user: %s", username);
        User userExists = userRepository.findByEmailOrUsername(username, username)
            .orElseThrow(() -> new ResourceNotFoundException("User", username));

        if(!userExists.isEnabled()) {
            throw new UnauthorizedException("User account is disabled");
        }

        if(!BCrypt.checkpw(password, userExists.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        LOG.infof("User %s authenticated successfully", username);

        String jwtToken = jwtService.generateToken(userExists);
        String refreshToken = "";

        Token newToken = new Token();
        newToken.setAccessToken(jwtToken);
        newToken.setRefreshToken(refreshToken);
        newToken.setUser(userExists);
        newToken.setLoggedOut(false);

        tokenRepository.invalidateTokensByUserId(userExists.getId());
        tokenRepository.persist(newToken);


        return new AuthResponseDTO(jwtToken, refreshToken, "Login successful");
    }

    @Override
    @Transactional
    public void logout(String email) {
        LOG.infof("Logging out user with email: %s", email);
        User user = userRepository.findByEmailOrUsername(email, email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));

        tokenRepository.invalidateTokensByUserId(user.getId());
        LOG.infof("User %s logged out successfully", email);
    }
}