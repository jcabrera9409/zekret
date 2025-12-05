# Error Handling System - Zekret Backend Quarkus

## Overview

The error handling system provides centralized and consistent exception management across the entire application, returning standardized HTTP responses and facilitating debugging.

## Components

### 1. Custom Exceptions

Located in `com.zekret.exception`, providing specific exceptions for different scenarios:

#### `ResourceNotFoundException` (HTTP 404)
- **Usage**: When a requested resource does not exist
- **Example**:
```java
throw new ResourceNotFoundException("User", "john@example.com");
throw new ResourceNotFoundException("Namespace not found");
```

#### `UnauthorizedException` (HTTP 401)
- **Usage**: Invalid credentials or unauthenticated user
- **Example**:
```java
throw new UnauthorizedException("Invalid credentials");
throw new UnauthorizedException("User account is disabled");
```

#### `ForbiddenException` (HTTP 403)
- **Usage**: Authenticated user without permissions for the resource
- **Example**:
```java
throw new ForbiddenException("You don't have permission to access this namespace");
```

#### `BadRequestException` (HTTP 400)
- **Usage**: Malformed request or invalid data
- **Example**:
```java
throw new BadRequestException("Invalid ZRN format");
throw new BadRequestException("Required field 'name' is missing");
```

#### `ConflictException` (HTTP 409)
- **Usage**: Conflict with the current state of the resource
- **Example**:
```java
throw new ConflictException("Email or username already in use");
throw new ConflictException("Namespace with this name already exists");
```

#### `InternalServerException` (HTTP 500)
- **Usage**: Internal server errors
- **Example**:
```java
throw new InternalServerException("Error generating JWT token", cause);
throw new InternalServerException("Database connection failed");
```

### 2. ErrorResponseDTO

DTO that standardizes all error responses:

```json
{
    "timestamp": "2024-12-04T10:30:45.123456",
    "status": 404,
    "error": "Not Found",
    "message": "User not found with identifier: john@example.com",
    "path": "/v1/users/john@example.com",
    "details": null  // Optional array for validations
}
```

### 3. GlobalExceptionHandler

Global handler that intercepts all exceptions using `@Provider` and `ExceptionMapper<Exception>`.

**Features**:
- ✅ Automatic interception of all exceptions
- ✅ Differentiated logging (WARN for client errors, ERROR for server errors)
- ✅ Standardized HTTP responses
- ✅ Special handling for `ConstraintViolationException` (Bean Validation)
- ✅ Automatic path context (`UriInfo`)
- ✅ Hiding internal details in production

## Service Implementation

### Before (using RuntimeException/IllegalArgumentException)
```java
User user = userRepository.findByEmail(email)
    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
```

### After (using custom exceptions)
```java
User user = userRepository.findByEmail(email)
    .orElseThrow(() -> new ResourceNotFoundException("User", email));
```

## Usage Examples by Service

### AuthServiceImpl
```java
// User not found
throw new ResourceNotFoundException("User", username);

// Account disabled
throw new UnauthorizedException("User account is disabled");

// Invalid credentials
throw new UnauthorizedException("Invalid credentials");
```

### UserServiceImpl
```java
// Email or username already exists
throw new ConflictException("Email or username already in use");
```

### NamespaceServiceImpl
```java
// User not found
throw new ResourceNotFoundException("User", userEmail);

// Namespace not found
throw new ResourceNotFoundException("Namespace", zrn);
```

### CredentialServiceImpl
```java
// User not found
throw new ResourceNotFoundException("User", userEmail);

// Credential not found
throw new ResourceNotFoundException("Credential", zrn);

// Credential type not found
throw new ResourceNotFoundException("CredentialType", credentialTypeZrn);
```

### JWTServiceImpl
```java
// Error generating token
throw new InternalServerException("Error generating JWT token", exception);
```

## Error Response Examples

### 404 - Resource Not Found
```json
{
    "timestamp": "2024-12-04T10:30:45.123456",
    "status": 404,
    "error": "Not Found",
    "message": "User not found with identifier: john@example.com",
    "path": "/v1/auth/login"
}
```

### 401 - Unauthorized
```json
{
    "timestamp": "2024-12-04T10:30:45.123456",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid credentials",
    "path": "/v1/auth/login"
}
```

### 409 - Conflict
```json
{
    "timestamp": "2024-12-04T10:30:45.123456",
    "status": 409,
    "error": "Conflict",
    "message": "Email or username already in use",
    "path": "/v1/users/register"
}
```

### 400 - Validation Failed
```json
{
    "timestamp": "2024-12-04T10:30:45.123456",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "path": "/v1/namespaces",
    "details": [
        "name must not be blank",
        "description size must be between 0 and 500"
    ]
}
```

## System Advantages

1. **Consistency**: All error responses have the same format
2. **Clarity**: Descriptive error messages and appropriate HTTP codes
3. **Traceability**: Automatic logging with appropriate levels
4. **Maintainability**: Easy to add new exception types
5. **Security**: Hiding internal details in production
6. **Debugging**: Contextual information (path, timestamp) in each error
7. **Validation**: Automatic support for Bean Validation

## Logging

The system automatically logs:

- **WARN** for client errors (4xx):
  ```
  WARN: Resource not found: User not found with identifier: john@example.com at path: /v1/auth/login
  ```

- **ERROR** for server errors (5xx):
  ```
  ERROR: Internal server error: Error generating JWT token at path: /v1/auth/login
  ```

## Bean Validation Integration

The handler automatically detects `@Valid` violations and converts them into structured responses:

```java
@POST
public Response createNamespace(@Valid NamespaceRequestDTO dto) {
    // If validation fails, GlobalExceptionHandler handles it automatically
}
```

## Best Practices

1. **Use the appropriate exception**: Choose the exception type that best represents the error
2. **Descriptive messages**: Provide clear and useful messages
3. **Don't expose internal details**: Avoid sensitive information in messages
4. **Consistent logging**: The handler takes care of logging, no need to duplicate it
5. **Useful context**: Include relevant identifiers (email, ZRN, etc.)

## Configuration

No additional configuration required. The `@Provider` is automatically detected by Quarkus and registered at application startup.

## Testing

To test error handling:

```java
@Test
public void testResourceNotFound() {
    given()
        .when().get("/v1/users/nonexistent@example.com")
        .then()
        .statusCode(404)
        .body("error", equalTo("Not Found"))
        .body("message", containsString("User not found"));
}
```
