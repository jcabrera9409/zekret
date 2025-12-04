package com.zekret.dto;

import java.time.LocalDateTime;

public record NamespaceResponseDTO (
    String name,
    String zrn,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
}
