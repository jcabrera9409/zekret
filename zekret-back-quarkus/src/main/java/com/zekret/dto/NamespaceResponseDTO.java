package com.zekret.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDateTime;
import java.util.List;

@RegisterForReflection
public record NamespaceResponseDTO (
    String name,
    String zrn,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<CredentialResponseDTO> credentials
) {
    
}
