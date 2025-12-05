package com.zekret.service.impl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zekret.dto.AuthResponseDTO;
import com.zekret.exception.ResourceNotFoundException;
import com.zekret.exception.UnauthorizedException;
import com.zekret.model.Token;
import com.zekret.model.User;
import com.zekret.repository.TokenRepository;
import com.zekret.repository.UserRepository;
import com.zekret.service.IJWTService;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class AuthServiceImplTest {

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    TokenRepository tokenRepository;

    @InjectMock
    IJWTService jwtService;

    @Inject
    AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        reset(userRepository, tokenRepository, jwtService);
        
        // Create a test user with BCrypt hashed password
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
        testUser.setEnabled(true);
    }

    @Test
    void testAuthenticate_Success() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(User.class)))
            .thenReturn("generated.jwt.token");
        doNothing().when(tokenRepository).invalidateTokensByUserId(anyLong());
        doNothing().when(tokenRepository).persist(any(Token.class));

        // Act
        AuthResponseDTO response = authService.authenticate("testuser", "password123");

        // Assert
        assertNotNull(response);
        assertEquals("generated.jwt.token", response.access_token());
        assertEquals("", response.refresh_token());
        assertEquals("Login successful", response.message());

        verify(userRepository, times(1)).findByEmailOrUsername("testuser", "testuser");
        verify(jwtService, times(1)).generateToken(testUser);
        verify(tokenRepository, times(1)).invalidateTokensByUserId(1L);
        verify(tokenRepository, times(1)).persist(any(Token.class));
    }

    @Test
    void testAuthenticate_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> authService.authenticate("nonexistent", "password")
        );

        assertTrue(exception.getMessage().contains("nonexistent"));
        verify(userRepository, times(1)).findByEmailOrUsername("nonexistent", "nonexistent");
        verify(jwtService, never()).generateToken(any(User.class));
        verify(tokenRepository, never()).persist(any(Token.class));
    }

    @Test
    void testAuthenticate_DisabledUser_ThrowsUnauthorizedException() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> authService.authenticate("testuser", "password123")
        );

        assertEquals("User account is disabled", exception.getMessage());
        verify(userRepository, times(1)).findByEmailOrUsername("testuser", "testuser");
        verify(jwtService, never()).generateToken(any(User.class));
        verify(tokenRepository, never()).persist(any(Token.class));
    }

    @Test
    void testAuthenticate_InvalidPassword_ThrowsUnauthorizedException() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> authService.authenticate("testuser", "wrongpassword")
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository, times(1)).findByEmailOrUsername("testuser", "testuser");
        verify(jwtService, never()).generateToken(any(User.class));
        verify(tokenRepository, never()).persist(any(Token.class));
    }

    @Test
    void testAuthenticate_WithEmail_Success() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(User.class)))
            .thenReturn("jwt.token.email");
        doNothing().when(tokenRepository).invalidateTokensByUserId(anyLong());
        doNothing().when(tokenRepository).persist(any(Token.class));

        // Act
        AuthResponseDTO response = authService.authenticate("test@example.com", "password123");

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.email", response.access_token());
        verify(userRepository, times(1)).findByEmailOrUsername("test@example.com", "test@example.com");
    }

    @Test
    void testAuthenticate_InvalidatesOldTokens() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(User.class)))
            .thenReturn("new.jwt.token");
        doNothing().when(tokenRepository).invalidateTokensByUserId(anyLong());
        doNothing().when(tokenRepository).persist(any(Token.class));

        // Act
        authService.authenticate("testuser", "password123");

        // Assert
        verify(tokenRepository, times(1)).invalidateTokensByUserId(testUser.getId());
    }

    @Test
    void testAuthenticate_PersistsNewToken() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(User.class)))
            .thenReturn("persisted.jwt.token");
        doNothing().when(tokenRepository).invalidateTokensByUserId(anyLong());

        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        doNothing().when(tokenRepository).persist(tokenCaptor.capture());

        // Act
        authService.authenticate("testuser", "password123");

        // Assert
        Token capturedToken = tokenCaptor.getValue();
        assertNotNull(capturedToken);
        assertEquals("persisted.jwt.token", capturedToken.getAccessToken());
        assertEquals("", capturedToken.getRefreshToken());
        assertFalse(capturedToken.isLoggedOut());
        assertEquals(testUser, capturedToken.getUser());
    }

    @Test
    void testLogout_Success() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).invalidateTokensByUserId(anyLong());

        // Act
        assertDoesNotThrow(() -> authService.logout("test@example.com"));

        // Assert
        verify(userRepository, times(1)).findByEmailOrUsername("test@example.com", "test@example.com");
        verify(tokenRepository, times(1)).invalidateTokensByUserId(testUser.getId());
    }

    @Test
    void testLogout_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> authService.logout("nonexistent@example.com")
        );

        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
        verify(userRepository, times(1)).findByEmailOrUsername("nonexistent@example.com", "nonexistent@example.com");
        verify(tokenRepository, never()).invalidateTokensByUserId(anyLong());
    }

    @Test
    void testLogout_WithUsername_Success() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).invalidateTokensByUserId(anyLong());

        // Act
        assertDoesNotThrow(() -> authService.logout("testuser"));

        // Assert
        verify(userRepository, times(1)).findByEmailOrUsername("testuser", "testuser");
        verify(tokenRepository, times(1)).invalidateTokensByUserId(testUser.getId());
    }

    @Test
    void testLogout_InvalidatesAllUserTokens() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).invalidateTokensByUserId(anyLong());

        // Act
        authService.logout("test@example.com");

        // Assert
        verify(tokenRepository, times(1)).invalidateTokensByUserId(eq(1L));
    }

    @Test
    void testAuthenticate_CaseSensitivePassword() {
        // Arrange - password with different case should fail
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> authService.authenticate("testuser", "Password123") // Different case
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testAuthenticate_EmptyPassword_ThrowsUnauthorizedException() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> authService.authenticate("testuser", "")
        );
        
        assertNotNull(exception);
    }
}
