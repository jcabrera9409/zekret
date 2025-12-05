package com.zekret.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CredentialRequestDTO (
    @NotBlank(message="Title is mandatory")
    @Size(min = 1, max = 255, message = "Title must be at most 255 characters")
    String title,
    
    String username,
    String password,
    String sshPublicKey,
    String sshPrivateKey,
    String secretText,
    String fileName,
    String fileContent,
    String notes,

    @NotBlank(message="Credential Type ZRN is mandatory")
    String credentialTypeZrn,

    @NotBlank(message="Namespace ZRN is mandatory")
    String namespaceZrn
) {}