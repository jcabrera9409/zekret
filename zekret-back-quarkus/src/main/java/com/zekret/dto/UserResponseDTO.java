package com.zekret.dto;

import java.time.LocalDateTime;

public record UserResponseDTO (
    String email,
    String username,
    LocalDateTime createdAt,
    boolean enabled
) {
    
}
