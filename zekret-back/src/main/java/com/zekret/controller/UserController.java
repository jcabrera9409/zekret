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
import com.zekret.model.User;
import com.zekret.service.IUserService;

@RestController
@RequestMapping("/v1/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private IUserService userService;
    
    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<APIResponseDTO<User>> registerUser(@RequestBody User request) {
        try {
            logger.info("Registering user: {}", request.getUsername());
            
            User newUser = userService.register(request);
            
            if (newUser != null) {
                logger.info("User registered successfully: {}", newUser.getUsername());
                APIResponseDTO<User> response = APIResponseDTO.success(
                    "Usuario registrado exitosamente", 
                    newUser, 
                    HttpStatus.CREATED.value()
                );
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.warn("User registration failed - user already exists: {}", request.getUsername());
                APIResponseDTO<User> response = APIResponseDTO.error(
                    "Usuario con este nombre o correo ya existe", 
                    HttpStatus.CONFLICT.value()
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error during user registration for user: {} - {}", request.getUsername(), e.getMessage());
            APIResponseDTO<User> response = APIResponseDTO.error(
                "User registration failed. Please try again.", 
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}