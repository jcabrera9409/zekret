package com.zekret.service;

import com.zekret.dto.AuthResponseDTO;

public interface IAuthService {

    /**
     * Authenticate user and generate token
     */
    AuthResponseDTO authenticate(String username, String password);

    /**
     * Logout user by invalidating their tokens
     */
    void logout(String email);
    
}