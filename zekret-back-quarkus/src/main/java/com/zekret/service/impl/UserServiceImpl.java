package com.zekret.service.impl;

import org.jboss.logging.Logger;

import com.zekret.dto.UserRequestDTO;
import com.zekret.dto.UserResponseDTO;
import com.zekret.exception.ConflictException;
import com.zekret.mapper.UserMapper;
import com.zekret.model.User;
import com.zekret.repository.UserRepository;
import com.zekret.service.IUserService;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserServiceImpl implements IUserService {
    private static final Logger LOG = Logger.getLogger(UserServiceImpl.class);
   
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponseDTO register(UserRequestDTO userRequestDTO) {
        LOG.infof("Registering user: %s", userRequestDTO.username());

        User user = userRepository.findByEmailOrUsername(userRequestDTO.email(), userRequestDTO.username())
            .orElse(null);
        if (user != null) {
            LOG.infof("Email or username already in use: %s, %s", userRequestDTO.email(), userRequestDTO.username());
            throw new ConflictException("Email or username already in use");
        }

        User newUser = UserMapper.toEntity(userRequestDTO);
        userRepository.persist(newUser);
        LOG.infof("User registered successfully: %s", newUser.getUsername());

        return UserMapper.toResponseDTO(newUser);
    }
}
