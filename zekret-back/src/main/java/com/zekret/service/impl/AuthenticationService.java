package com.zekret.service.impl;

import java.util.List;

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

    public AuthenticationResponseDTO authenticate(User request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User usuario = repository.findByEmailOrUsername(request.getEmail(), request.getUsername()).orElseThrow();
        
        if(usuario.isEnabled()) {
        	String accessToken = jwtService.generateAccessToken(usuario);
            String refreshToken = jwtService.generateRefreshToken(usuario);

            revokeAllTokenByVendedor(usuario);
            saveUserToken(accessToken, refreshToken, usuario);

            return new AuthenticationResponseDTO(accessToken, refreshToken, "User authenticated successfully.");
        } else {
        	return new AuthenticationResponseDTO(null, null, "Your account is not enabled. Please contact support.");
        }
    }

    public void revokeAllTokenByVendedor(User usuario) {
        List<Token> validTokens = tokenRepository.findByUserIdAndLoggedOutFalse(usuario.getId());
        if(validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t-> {
            t.setLoggedOut(true);
        });

        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(String accessToken, String refreshToken, User usuario) {
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(usuario);
        tokenRepository.save(token);
    }

    public int modificarPasswordPorId(Long id, String password) {
		int filasActualizadas = repository.updatePasswordById(id, passwordEncoder.encode(password));
		return filasActualizadas;
	}
}
