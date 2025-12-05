package com.zekret.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDateTime;

@RegisterForReflection
public record APIResponseDTO<T>(
    boolean success,
    String message,
    T data,
    int statusCode,
    LocalDateTime timestamp
) {
    public static <T> APIResponseDTO<T> success(String message, T data, int statusCode) {
        return new APIResponseDTO<>(true, message, data, statusCode, LocalDateTime.now());
    }

    public static <T> APIResponseDTO<T> error(String message, int statusCode) {
        return new APIResponseDTO<>(false, message, null, statusCode, LocalDateTime.now());
    }
}