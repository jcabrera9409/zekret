package com.zekret.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record CredentialTypeResponseDTO (
    String zrn,
    String name
) {}