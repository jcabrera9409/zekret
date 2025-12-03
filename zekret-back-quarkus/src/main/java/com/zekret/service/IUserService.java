package com.zekret.service;

import com.zekret.dto.UserRequestDTO;
import com.zekret.dto.UserResponseDTO;

public interface IUserService {
    UserResponseDTO register(UserRequestDTO userRequestDTO);
}
