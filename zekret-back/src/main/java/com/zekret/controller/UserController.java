package com.zekret.controller;

import java.time.LocalDateTime;

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
    
    @PostMapping("/register")
    public ResponseEntity<APIResponseDTO<User>> registerUser(@RequestBody User request) {
    	logger.info("Registering user: {}", request.getUsername());
    	request.setCreatedAt(LocalDateTime.now());
    	
    	User newUser = userService.register(request);
    	APIResponseDTO<User> response = (newUser != null) 
			? APIResponseDTO.success("User registered successfully", newUser, HttpStatus.CREATED.value())
			: APIResponseDTO.error("User registration failed", HttpStatus.BAD_REQUEST.value());
    	logger.info("User registration message: {}", response.getMessage());
		
		return ResponseEntity.status(response.getStatusCode())
				.body(response);
	}
}