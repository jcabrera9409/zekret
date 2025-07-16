package com.zekret.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.zekret.model.User;
import com.zekret.repo.IGenericRepo;
import com.zekret.repo.IUserRepo;
import com.zekret.service.IUserService;

@Service
public class UserServiceImpl extends CRUDImpl<User, Long> implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final IUserRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public UserServiceImpl(IUserRepo userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected IGenericRepo<User, Long> getRepo() {
        return userRepository;
    }
    
    @Override
    public User register(User user) {
        logger.info("Registering user: {}", user.getUsername());
        
        // Check if user already exists
        User existingUser = userRepository.findByEmailOrUsername(user.getEmail(), user.getUsername()).orElse(null);
        if (existingUser != null) {
            logger.warn("User with email {} or username {} already exists", user.getEmail(), user.getUsername());
            return null; 
        }
        
        // Encode password and save user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Timestamps are automatically handled by @CreationTimestamp and @UpdateTimestamp
        logger.info("Saving user - timestamps will be set automatically");
        
        return userRepository.save(user);
    }
}