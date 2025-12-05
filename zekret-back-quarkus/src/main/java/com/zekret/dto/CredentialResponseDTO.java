package com.zekret.dto;

import java.time.LocalDateTime;

public record CredentialResponseDTO (
    String zrn,
    String tite,
    String usename,
    String pasword,
    String sshublicKey,
    String sshrivateKey,
    String secetText,
    String filName,
    String filContent,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    CredentialTypeResponseDTO credentialType,
    NamespaceResponseDTO namespace  
) {}