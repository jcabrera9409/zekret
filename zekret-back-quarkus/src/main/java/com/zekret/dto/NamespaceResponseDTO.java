package com.zekret.dto;

import java.time.LocalDateTime;
import java.util.List;

public record NamespaceResponseDTO (
    String name,
    String zrn,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<CredentialResponseDTO> credentials
) {
    
}
