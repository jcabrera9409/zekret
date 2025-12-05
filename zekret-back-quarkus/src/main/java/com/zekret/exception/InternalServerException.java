package com.zekret.exception;

/**
 * Excepción lanzada cuando ocurre un error interno del servidor.
 * Mapea a HTTP 500 (Internal Server Error).
 */
public class InternalServerException extends RuntimeException {
    
    public InternalServerException(String message) {
        super(message);
    }
    
    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
