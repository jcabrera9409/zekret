package com.zekret.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AuthResponseDTO(String access_token, String refresh_token, String message) {
}