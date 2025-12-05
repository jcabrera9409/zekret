package com.zekret.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDateTime;

@RegisterForReflection
public record CredentialResponseDTO (
    String zrn,
    String title,
    String username,
    String password,
    String sshPublicKey,
    String sshPrivateKey,
    String secretText,
    String fileName,
    String fileContent,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    CredentialTypeResponseDTO credentialType,
    NamespaceResponseDTO namespace  
) {}