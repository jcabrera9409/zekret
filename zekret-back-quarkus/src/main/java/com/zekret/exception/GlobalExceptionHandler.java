package com.zekret.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.zekret.dto.ErrorResponseDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Manejador global de excepciones para la aplicación.
 * Intercepta todas las excepciones y las convierte en respuestas HTTP estandarizadas.
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {
    
    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);
    
    @Context
    UriInfo uriInfo;
    
    @Override
    public Response toResponse(Exception exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "unknown";
        
        // Manejo de excepciones personalizadas
        if (exception instanceof ResourceNotFoundException ex) {
            return handleResourceNotFoundException(ex, path);
        }
        
        if (exception instanceof UnauthorizedException ex) {
            return handleUnauthorizedException(ex, path);
        }
        
        if (exception instanceof ForbiddenException ex) {
            return handleForbiddenException(ex, path);
        }
        
        if (exception instanceof BadRequestException ex) {
            return handleBadRequestException(ex, path);
        }
        
        if (exception instanceof ConflictException ex) {
            return handleConflictException(ex, path);
        }
        
        if (exception instanceof InternalServerException ex) {
            return handleInternalServerException(ex, path);
        }
        
        // Manejo de validación de Bean Validation
        if (exception instanceof ConstraintViolationException ex) {
            return handleConstraintViolationException(ex, path);
        }
        
        // Manejo de excepciones genéricas
        return handleGenericException(exception, path);
    }
    
    private Response handleResourceNotFoundException(ResourceNotFoundException ex, String path) {
        LOG.warnf("Resource not found: %s at path: %s", ex.getMessage(), path);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            Response.Status.NOT_FOUND.getStatusCode(),
            "Not Found",
            ex.getMessage(),
            path
        );
        return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
    }
    
    private Response handleUnauthorizedException(UnauthorizedException ex, String path) {
        LOG.warnf("Unauthorized access: %s at path: %s", ex.getMessage(), path);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            Response.Status.UNAUTHORIZED.getStatusCode(),
            "Unauthorized",
            ex.getMessage(),
            path
        );
        return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse).build();
    }
    
    private Response handleForbiddenException(ForbiddenException ex, String path) {
        LOG.warnf("Forbidden access: %s at path: %s", ex.getMessage(), path);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            Response.Status.FORBIDDEN.getStatusCode(),
            "Forbidden",
            ex.getMessage(),
            path
        );
        return Response.status(Response.Status.FORBIDDEN).entity(errorResponse).build();
    }
    
    private Response handleBadRequestException(BadRequestException ex, String path) {
        LOG.warnf("Bad request: %s at path: %s", ex.getMessage(), path);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "Bad Request",
            ex.getMessage(),
            path
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }
    
    private Response handleConflictException(ConflictException ex, String path) {
        LOG.warnf("Conflict: %s at path: %s", ex.getMessage(), path);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            Response.Status.CONFLICT.getStatusCode(),
            "Conflict",
            ex.getMessage(),
            path
        );
        return Response.status(Response.Status.CONFLICT).entity(errorResponse).build();
    }
    
    private Response handleInternalServerException(InternalServerException ex, String path) {
        LOG.errorf(ex, "Internal server error: %s at path: %s", ex.getMessage(), path);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Internal Server Error",
            ex.getMessage(),
            path
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
    }
    
    private Response handleConstraintViolationException(ConstraintViolationException ex, String path) {
        LOG.warnf("Validation failed at path: %s", path);
        
        List<String> violations = ex.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "Bad Request",
            "Validation failed",
            path,
            violations
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }
    
    private Response handleGenericException(Exception ex, String path) {
        LOG.errorf(ex, "Unhandled exception at path: %s", path);
        
        // En producción, no exponer detalles internos
        String message = "An unexpected error occurred";
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Internal Server Error",
            message,
            path
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
    }
}
