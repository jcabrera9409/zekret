package com.zekret.mapper;

import com.zekret.dto.UserRequestDTO;
import com.zekret.dto.UserResponseDTO;
import com.zekret.model.User;

public class UserMapper {
    public static User toEntity(UserRequestDTO dto) {
        User user = new User();
        user.setEmail(dto.email());
        user.setUsername(dto.username());
        user.setPassword(dto.password());
        return user;
    }

    public static UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
            user.getEmail(),
            user.getUsername(),
            user.getCreatedAt(),
            user.isEnabled()
        );
    }
}
