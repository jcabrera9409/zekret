package com.zekret.exception;

/**
 * Excepción lanzada cuando la solicitud del cliente es inválida o contiene datos incorrectos.
 * Mapea a HTTP 400 (Bad Request).
 */
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
