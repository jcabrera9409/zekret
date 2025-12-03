package com.zekret.service;

import com.zekret.model.User;

public interface IJWTService {

    /**
     * Generate JWT token for the given user.
     */
    String generateToken(User user);
}