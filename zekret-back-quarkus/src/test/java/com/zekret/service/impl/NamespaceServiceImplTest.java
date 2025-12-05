package com.zekret.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zekret.dto.NamespaceRequestDTO;
import com.zekret.dto.NamespaceResponseDTO;
import com.zekret.exception.ResourceNotFoundException;
import com.zekret.model.Namespace;
import com.zekret.model.User;
import com.zekret.repository.NamespaceRepository;
import com.zekret.repository.UserRepository;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class NamespaceServiceImplTest {

    @InjectMock
    NamespaceRepository namespaceRepository;

    @InjectMock
    UserRepository userRepository;

    @Inject
    NamespaceServiceImpl namespaceService;

    private User testUser;
    private Namespace testNamespace;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        reset(namespaceRepository, userRepository);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        
        testNamespace = new Namespace();
        testNamespace.setId(1L);
        testNamespace.setName("Production");
        testNamespace.setZrn("zrn:zekret:namespace:20250715:prod-123");
        testNamespace.setDescription("Production environment");
        testNamespace.setUser(testUser);
        testNamespace.prePersist();
        testNamespace.setCredentials(new ArrayList<>());
    }

    @Test
    void testGetNamespacesByUserEmail_Success() {
        // Arrange
        Namespace ns2 = new Namespace();
        ns2.setId(2L);
        ns2.setName("Development");
        ns2.setZrn("zrn:zekret:namespace:20250715:dev-456");
        ns2.setDescription("Dev environment");
        ns2.setUser(testUser);
        ns2.prePersist();
        ns2.setCredentials(new ArrayList<>());

        List<Namespace> namespaces = Arrays.asList(testNamespace, ns2);

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(namespaceRepository.findByUserId(anyLong()))
            .thenReturn(namespaces);

        // Act
        List<NamespaceResponseDTO> result = namespaceService.getNamespacesByUserEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Production", result.get(0).name());
        assertEquals("Development", result.get(1).name());

        verify(userRepository, times(1)).findByEmailOrUsername("test@example.com", "test@example.com");
        verify(namespaceRepository, times(1)).findByUserId(1L);
    }

    @Test
    void testGetNamespacesByUserEmail_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> namespaceService.getNamespacesByUserEmail("nonexistent@example.com")
        );

        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
        verify(namespaceRepository, never()).findByUserId(anyLong());
    }

    @Test
    void testGetNamespacesByUserEmail_EmptyList() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(namespaceRepository.findByUserId(anyLong()))
            .thenReturn(new ArrayList<>());

        // Act
        List<NamespaceResponseDTO> result = namespaceService.getNamespacesByUserEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetNamespaceByZrnAndUserEmail_Success() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(namespaceRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(testNamespace));

        // Act
        NamespaceResponseDTO result = namespaceService.getNamespaceByZrnAndUserEmail(
            testNamespace.getZrn(), 
            "test@example.com"
        );

        // Assert
        assertNotNull(result);
        assertEquals("Production", result.name());
        assertEquals(testNamespace.getZrn(), result.zrn());
        assertEquals("Production environment", result.description());

        verify(userRepository, times(1)).findByEmailOrUsername("test@example.com", "test@example.com");
        verify(namespaceRepository, times(1)).findByZrnAndUserId(testNamespace.getZrn(), 1L);
    }

    @Test
    void testGetNamespaceByZrnAndUserEmail_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> namespaceService.getNamespaceByZrnAndUserEmail("some-zrn", "nonexistent@example.com")
        );

        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
        verify(namespaceRepository, never()).findByZrnAndUserId(anyString(), anyLong());
    }

    @Test
    void testGetNamespaceByZrnAndUserEmail_NamespaceNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(namespaceRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.empty());

        String nonExistentZrn = "zrn:zekret:namespace:20250715:nonexistent";

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> namespaceService.getNamespaceByZrnAndUserEmail(nonExistentZrn, "test@example.com")
        );

        assertTrue(exception.getMessage().contains(nonExistentZrn));
        verify(namespaceRepository, times(1)).findByZrnAndUserId(nonExistentZrn, 1L);
    }

    @Test
    void testRegisterNamespace_Success() {
        // Arrange
        NamespaceRequestDTO requestDTO = new NamespaceRequestDTO(
            "Staging",
            "Staging environment"
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        
        // Capture and initialize credentials list
        ArgumentCaptor<Namespace> nsCaptor = ArgumentCaptor.forClass(Namespace.class);
        doAnswer(invocation -> {
            Namespace ns = invocation.getArgument(0);
            if (ns.getCredentials() == null) {
                ns.setCredentials(new ArrayList<>());
            }
            return null;
        }).when(namespaceRepository).persist(nsCaptor.capture());

        // Act
        NamespaceResponseDTO result = namespaceService.registerNamespace("test@example.com", requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Staging", result.name());
        assertEquals("Staging environment", result.description());
        assertNotNull(result.zrn());
        assertTrue(result.zrn().startsWith("zrn:zekret:namespace:"));

        verify(userRepository, times(1)).findByEmailOrUsername("test@example.com", "test@example.com");
        verify(namespaceRepository, times(1)).persist(any(Namespace.class));
    }

    @Test
    void testRegisterNamespace_UserNotFound_ThrowsException() {
        // Arrange
        NamespaceRequestDTO requestDTO = new NamespaceRequestDTO("Test", "Test description");

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> namespaceService.registerNamespace("nonexistent@example.com", requestDTO)
        );

        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
        verify(namespaceRepository, never()).persist(any(Namespace.class));
    }

    @Test
    void testRegisterNamespace_AssignsUserCorrectly() {
        // Arrange
        NamespaceRequestDTO requestDTO = new NamespaceRequestDTO("Test NS", "Test description");

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));

        ArgumentCaptor<Namespace> namespaceCaptor = ArgumentCaptor.forClass(Namespace.class);
        doAnswer(invocation -> {
            Namespace ns = invocation.getArgument(0);
            if (ns.getCredentials() == null) {
                ns.setCredentials(new ArrayList<>());
            }
            return null;
        }).when(namespaceRepository).persist(namespaceCaptor.capture());

        // Act
        namespaceService.registerNamespace("test@example.com", requestDTO);

        // Assert
        Namespace capturedNamespace = namespaceCaptor.getValue();
        assertNotNull(capturedNamespace);
        assertEquals(testUser, capturedNamespace.getUser());
        assertNotNull(capturedNamespace.getZrn());
    }

    @Test
    void testUpdateNamespace_Success() {
        // Arrange
        NamespaceRequestDTO updateDTO = new NamespaceRequestDTO(
            "Updated Production",
            "Updated production environment"
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(namespaceRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(testNamespace));
        doNothing().when(namespaceRepository).persist(any(Namespace.class));

        // Act
        NamespaceResponseDTO result = namespaceService.updateNamespace(
            "test@example.com", 
            testNamespace.getZrn(), 
            updateDTO
        );

        // Assert
        assertNotNull(result);
        assertEquals("Updated Production", result.name());
        assertEquals("Updated production environment", result.description());
        assertEquals(testNamespace.getZrn(), result.zrn());

        verify(namespaceRepository, times(1)).findByZrnAndUserId(testNamespace.getZrn(), 1L);
        verify(namespaceRepository, times(1)).persist(any(Namespace.class));
    }

    @Test
    void testUpdateNamespace_UserNotFound_ThrowsException() {
        // Arrange
        NamespaceRequestDTO updateDTO = new NamespaceRequestDTO("Updated", "Updated description");

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> namespaceService.updateNamespace("nonexistent@example.com", "some-zrn", updateDTO)
        );

        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
        verify(namespaceRepository, never()).findByZrnAndUserId(anyString(), anyLong());
    }

    @Test
    void testUpdateNamespace_NamespaceNotFound_ThrowsException() {
        // Arrange
        NamespaceRequestDTO updateDTO = new NamespaceRequestDTO("Updated", "Updated description");
        String nonExistentZrn = "zrn:zekret:namespace:20250715:nonexistent";

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(namespaceRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> namespaceService.updateNamespace("test@example.com", nonExistentZrn, updateDTO)
        );

        assertTrue(exception.getMessage().contains(nonExistentZrn));
        verify(namespaceRepository, never()).persist(any(Namespace.class));
    }

    @Test
    void testDeleteNamespaceByZrnAndUserEmail_Success() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(namespaceRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(testNamespace));
        doNothing().when(namespaceRepository).delete(any(Namespace.class));

        // Act
        assertDoesNotThrow(() -> 
            namespaceService.deleteNamespaceByZrnAndUserEmail(testNamespace.getZrn(), "test@example.com")
        );

        // Assert
        verify(userRepository, times(1)).findByEmailOrUsername("test@example.com", "test@example.com");
        verify(namespaceRepository, times(1)).findByZrnAndUserId(testNamespace.getZrn(), 1L);
        verify(namespaceRepository, times(1)).delete(testNamespace);
    }

    @Test
    void testDeleteNamespaceByZrnAndUserEmail_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> namespaceService.deleteNamespaceByZrnAndUserEmail("some-zrn", "nonexistent@example.com")
        );

        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
        verify(namespaceRepository, never()).delete(any(Namespace.class));
    }

    @Test
    void testDeleteNamespaceByZrnAndUserEmail_NamespaceNotFound_ThrowsException() {
        // Arrange
        String nonExistentZrn = "zrn:zekret:namespace:20250715:nonexistent";

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(namespaceRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> namespaceService.deleteNamespaceByZrnAndUserEmail(nonExistentZrn, "test@example.com")
        );

        assertTrue(exception.getMessage().contains(nonExistentZrn));
        verify(namespaceRepository, never()).delete(any(Namespace.class));
    }
}
