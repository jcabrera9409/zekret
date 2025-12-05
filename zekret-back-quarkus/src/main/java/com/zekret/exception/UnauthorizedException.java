package com.zekret.exception;

/**
 * Excepción lanzada cuando un usuario no está autenticado o sus credenciales son inválidas.
 * Mapea a HTTP 401 (Unauthorized).
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
