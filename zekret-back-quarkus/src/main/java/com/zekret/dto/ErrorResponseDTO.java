package com.zekret.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuestas de error estandarizadas.
 * Proporciona información consistente sobre errores HTTP.
 */
public record ErrorResponseDTO(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<String> details
) {
    
    /**
     * Constructor simplificado sin detalles adicionales
     */
    public ErrorResponseDTO(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }
    
    /**
     * Constructor con detalles adicionales
     */
    public ErrorResponseDTO(int status, String error, String message, String path, List<String> details) {
        this(LocalDateTime.now(), status, error, message, path, details);
    }
}
