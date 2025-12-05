package com.zekret.exception;

/**
 * Excepción lanzada cuando un usuario autenticado intenta acceder a un recurso
 * para el cual no tiene permisos.
 * Mapea a HTTP 403 (Forbidden).
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
