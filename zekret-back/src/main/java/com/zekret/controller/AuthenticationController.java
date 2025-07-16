package com.zekret.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zekret.dto.APIResponseDTO;
import com.zekret.dto.AuthenticationResponseDTO;
import com.zekret.model.User;
import com.zekret.service.impl.AuthenticationService;

@RestController
@RequestMapping("/v1/auth")
public class AuthenticationController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @PostMapping("/login")
    public ResponseEntity<APIResponseDTO<AuthenticationResponseDTO>> login(@RequestBody User request) {
        logger.info("Login attempt for user: {}", request.getUsername());
        
        try {
            AuthenticationResponseDTO authResponse = authenticationService.authenticate(request);
            
            if (authResponse.getAccessToken() != null) {
                logger.info("Login successful for user: {}", request.getUsername());
                APIResponseDTO<AuthenticationResponseDTO> response = APIResponseDTO.success(
                    authResponse.getMessage(), 
                    authResponse, 
                    HttpStatus.OK.value()
                );
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Login failed for user: {} - {}", request.getUsername(), authResponse.getMessage());
                APIResponseDTO<AuthenticationResponseDTO> response = APIResponseDTO.error(
                    authResponse.getMessage(), 
                    HttpStatus.UNAUTHORIZED.value()
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error during login for user: {} - {}", request.getUsername(), e.getMessage());
            APIResponseDTO<AuthenticationResponseDTO> response = APIResponseDTO.error(
                "Authentication failed. Please check your credentials.", 
                HttpStatus.UNAUTHORIZED.value()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
