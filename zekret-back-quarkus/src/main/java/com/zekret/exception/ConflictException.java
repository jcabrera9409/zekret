package com.zekret.exception;

/**
 * Excepción lanzada cuando hay un conflicto con el estado actual del recurso.
 * Por ejemplo, cuando se intenta crear un recurso que ya existe.
 * Mapea a HTTP 409 (Conflict).
 */
public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
