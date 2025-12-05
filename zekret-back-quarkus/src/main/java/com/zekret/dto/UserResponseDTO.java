package com.zekret.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDateTime;

@RegisterForReflection
public record UserResponseDTO (
    String email,
    String username,
    LocalDateTime createdAt,
    boolean enabled
) {
    
}
