package com.zekret.dto;

import java.time.LocalDateTime;

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