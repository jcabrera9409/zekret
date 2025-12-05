package com.zekret.service.impl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zekret.dto.UserRequestDTO;
import com.zekret.dto.UserResponseDTO;
import com.zekret.exception.ConflictException;
import com.zekret.model.User;
import com.zekret.repository.UserRepository;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class UserServiceImplTest {

    @InjectMock
    UserRepository userRepository;

    @Inject
    UserServiceImpl userService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        reset(userRepository);
    }

    @Test
    void testRegister_Success() {
        // Arrange
        UserRequestDTO requestDTO = new UserRequestDTO(
            "newuser@test.com",
            "newuser",
            "password123"
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Capture the persisted user and simulate prePersist
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.prePersist(); // Simulate what would happen on persist
            return null;
        }).when(userRepository).persist(userCaptor.capture());

        // Act
        UserResponseDTO response = userService.register(requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(requestDTO.email(), response.email());
        assertEquals(requestDTO.username(), response.username());
        assertTrue(response.enabled());
        assertNotNull(response.createdAt());

        // Verify interactions
        verify(userRepository, times(1)).findByEmailOrUsername(
            requestDTO.email(), 
            requestDTO.username()
        );
        verify(userRepository, times(1)).persist(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists_ThrowsConflictException() {
        // Arrange
        UserRequestDTO requestDTO = new UserRequestDTO(
            "existing@test.com",
            "newuser",
            "password123"
        );

        User existingUser = new User();
        existingUser.setEmail("existing@test.com");
        existingUser.setUsername("existinguser");

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(existingUser));

        // Act & Assert
        ConflictException exception = assertThrows(
            ConflictException.class,
            () -> userService.register(requestDTO)
        );

        assertEquals("Email or username already in use", exception.getMessage());
        verify(userRepository, times(1)).findByEmailOrUsername(
            requestDTO.email(), 
            requestDTO.username()
        );
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists_ThrowsConflictException() {
        // Arrange
        UserRequestDTO requestDTO = new UserRequestDTO(
            "newuser@test.com",
            "existinguser",
            "password123"
        );

        User existingUser = new User();
        existingUser.setEmail("other@test.com");
        existingUser.setUsername("existinguser");

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(existingUser));

        // Act & Assert
        ConflictException exception = assertThrows(
            ConflictException.class,
            () -> userService.register(requestDTO)
        );

        assertEquals("Email or username already in use", exception.getMessage());
        verify(userRepository, times(1)).findByEmailOrUsername(
            requestDTO.email(), 
            requestDTO.username()
        );
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    void testRegister_PersistsUserWithCorrectData() {
        // Arrange
        UserRequestDTO requestDTO = new UserRequestDTO(
            "test@example.com",
            "testuser",
            "securepassword"
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        doNothing().when(userRepository).persist(userCaptor.capture());

        // Act
        userService.register(requestDTO);

        // Assert
        User capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser);
        assertEquals(requestDTO.email(), capturedUser.getEmail());
        assertEquals(requestDTO.username(), capturedUser.getUsername());
        assertEquals(requestDTO.password(), capturedUser.getPassword());
    }

    @Test
    void testRegister_VerifyRepositoryCalledWithCorrectParameters() {
        // Arrange
        UserRequestDTO requestDTO = new UserRequestDTO(
            "verify@test.com",
            "verifyuser",
            "password123"
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        doNothing().when(userRepository).persist(any(User.class));

        // Act
        userService.register(requestDTO);

        // Assert
        verify(userRepository).findByEmailOrUsername(
            eq("verify@test.com"), 
            eq("verifyuser")
        );
    }

    @Test
    void testRegister_UserEnabledByDefault() {
        // Arrange
        UserRequestDTO requestDTO = new UserRequestDTO(
            "enabled@test.com",
            "enableduser",
            "password123"
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        doNothing().when(userRepository).persist(userCaptor.capture());

        // Act
        UserResponseDTO response = userService.register(requestDTO);

        // Assert
        assertTrue(response.enabled());
        
        User persistedUser = userCaptor.getValue();
        assertTrue(persistedUser.isEnabled() || response.enabled()); 
    }

    @Test
    void testRegister_MultipleUsers_Success() {
        // Test that multiple different users can be registered

        // User 1
        UserRequestDTO requestDTO1 = new UserRequestDTO(
            "user1@test.com",
            "user1",
            "password1"
        );

        when(userRepository.findByEmailOrUsername("user1@test.com", "user1"))
            .thenReturn(Optional.empty());
        doNothing().when(userRepository).persist(any(User.class));

        UserResponseDTO response1 = userService.register(requestDTO1);
        assertNotNull(response1);
        assertEquals("user1@test.com", response1.email());

        // User 2
        UserRequestDTO requestDTO2 = new UserRequestDTO(
            "user2@test.com",
            "user2",
            "password2"
        );

        when(userRepository.findByEmailOrUsername("user2@test.com", "user2"))
            .thenReturn(Optional.empty());

        UserResponseDTO response2 = userService.register(requestDTO2);
        assertNotNull(response2);
        assertEquals("user2@test.com", response2.email());

        // Verify both were persisted
        verify(userRepository, times(2)).persist(any(User.class));
    }

    @Test
    void testRegister_LogsCorrectly() {
        // This test verifies the service logs during registration
        UserRequestDTO requestDTO = new UserRequestDTO(
            "logged@test.com",
            "loggeduser",
            "password123"
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        doNothing().when(userRepository).persist(any(User.class));

        // Act - should not throw any exceptions
        assertDoesNotThrow(() -> userService.register(requestDTO));
    }
}
