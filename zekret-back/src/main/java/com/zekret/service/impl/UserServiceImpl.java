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

    @Autowired
	private final IUserRepo userRepository;

    @Autowired
	private PasswordEncoder passwordEncoder;
    
	public UserServiceImpl(IUserRepo userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	protected IGenericRepo<User, Long> getRepo() {
		logger.info("Returning user repository");
		return userRepository;
	}
	
	@Override
	public User register(User user) {
		logger.info("Registering user: {}", user.getUsername());
		User existingUser = userRepository.findByEmailOrUsername(user.getEmail(), user.getUsername()).orElse(null);
		if (existingUser != null) {
			logger.warn("User with email {} or username {} already exists.", user.getEmail(), user.getUsername());
			return null; 
		}
		user.setCreatedAt(java.time.LocalDateTime.now());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}
}