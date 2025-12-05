package com.zekret.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en el sistema.
 * Mapea a HTTP 404 (Not Found).
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier));
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
