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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zekret.dto.CredentialRequestDTO;
import com.zekret.dto.CredentialResponseDTO;
import com.zekret.exception.ResourceNotFoundException;
import com.zekret.model.Credential;
import com.zekret.model.CredentialType;
import com.zekret.model.Namespace;
import com.zekret.model.User;
import com.zekret.repository.CredentialRepository;
import com.zekret.repository.CredentialTypeRepository;
import com.zekret.repository.NamespaceRepository;
import com.zekret.repository.UserRepository;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class CredentialServiceImplTest {

    @InjectMock
    CredentialRepository credentialRepository;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    CredentialTypeRepository credentialTypeRepository;

    @InjectMock
    NamespaceRepository namespaceRepository;

    @Inject
    CredentialServiceImpl credentialService;

    private User testUser;
    private Namespace testNamespace;
    private CredentialType testCredentialType;
    private Credential testCredential;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        reset(credentialRepository, userRepository, credentialTypeRepository, namespaceRepository);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        testNamespace = new Namespace();
        testNamespace.setId(1L);
        testNamespace.setName("Production");
        testNamespace.setZrn("zrn:zekret:namespace:20250715:prod-123");
        testNamespace.setDescription("Production namespace");
        testNamespace.setUser(testUser);
        testNamespace.prePersist();
        testNamespace.setCredentials(new ArrayList<>());

        testCredentialType = new CredentialType();
        testCredentialType.setId(1L);
        testCredentialType.setZrn("ssh_credential");
        testCredentialType.setName("SSH Credential");

        testCredential = new Credential();
        testCredential.setId(1L);
        testCredential.setTitle("Production SSH");
        testCredential.setZrn("zrn:zekret:credential:20250715:cred-123");
        testCredential.setUsername("admin");
        testCredential.setPassword("password123");
        testCredential.setCredentialType(testCredentialType);
        testCredential.setNamespace(testNamespace);
        testCredential.setUser(testUser);
        testCredential.prePersist();
    }

    @Test
    void testGetCredentialsByNamespaceAndUserId_Success() {
        // Arrange
        Credential cred2 = new Credential();
        cred2.setId(2L);
        cred2.setTitle("Another Credential");
        cred2.setZrn("zrn:zekret:credential:20250715:cred-456");
        cred2.setCredentialType(testCredentialType);
        cred2.setNamespace(testNamespace);
        cred2.setUser(testUser);
        cred2.prePersist();

        List<Credential> credentials = Arrays.asList(testCredential, cred2);

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialRepository.findByNamespaceZrnAndUserId(anyString(), anyLong()))
            .thenReturn(credentials);

        // Act
        List<CredentialResponseDTO> result = credentialService.getCredentialsByNamespaceAndUserId(
            testNamespace.getZrn(), 
            "test@example.com"
        );

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Production SSH", result.get(0).tite());
        assertEquals("Another Credential", result.get(1).tite());

        verify(userRepository, times(1)).findByEmailOrUsername("test@example.com", "test@example.com");
        verify(credentialRepository, times(1)).findByNamespaceZrnAndUserId(testNamespace.getZrn(), 1L);
    }

    @Test
    void testGetCredentialsByNamespaceAndUserId_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> credentialService.getCredentialsByNamespaceAndUserId("some-zrn", "nonexistent@example.com")
        );

        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
        verify(credentialRepository, never()).findByNamespaceZrnAndUserId(anyString(), anyLong());
    }

    @Test
    void testGetCredentialsByNamespaceAndUserId_EmptyList() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialRepository.findByNamespaceZrnAndUserId(anyString(), anyLong()))
            .thenReturn(new ArrayList<>());

        // Act
        List<CredentialResponseDTO> result = credentialService.getCredentialsByNamespaceAndUserId(
            testNamespace.getZrn(), 
            "test@example.com"
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCredentialByZrnAndUserEmail_Success() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(testCredential));

        // Act
        CredentialResponseDTO result = credentialService.getCredentialByZrnAndUserEmail(
            testCredential.getZrn(), 
            "test@example.com"
        );

        // Assert
        assertNotNull(result);
        assertEquals("Production SSH", result.tite());
        assertEquals(testCredential.getZrn(), result.zrn());
        assertEquals("admin", result.usename());

        verify(userRepository, times(1)).findByEmailOrUsername("test@example.com", "test@example.com");
        verify(credentialRepository, times(1)).findByZrnAndUserId(testCredential.getZrn(), 1L);
    }

    @Test
    void testGetCredentialByZrnAndUserEmail_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> credentialService.getCredentialByZrnAndUserEmail("some-zrn", "nonexistent@example.com")
        );

        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
        verify(credentialRepository, never()).findByZrnAndUserId(anyString(), anyLong());
    }

    @Test
    void testGetCredentialByZrnAndUserEmail_CredentialNotFound_ThrowsException() {
        // Arrange
        String nonExistentZrn = "zrn:zekret:credential:20250715:nonexistent";

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> credentialService.getCredentialByZrnAndUserEmail(nonExistentZrn, "test@example.com")
        );

        assertTrue(exception.getMessage().contains(nonExistentZrn));
    }

    @Test
    void testRegisterCredential_Success() {
        // Arrange
        CredentialRequestDTO requestDTO = new CredentialRequestDTO(
            "New SSH Key",
            "root",
            "rootpass",
            "ssh-rsa AAAAB3...",
            "-----BEGIN RSA PRIVATE KEY-----...",
            null,
            null,
            null,
            "Important server",
            "ssh_credential",
            testNamespace.getZrn()
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialTypeRepository.findByZrn(anyString()))
            .thenReturn(Optional.of(testCredentialType));
        when(namespaceRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(testNamespace));
        doNothing().when(credentialRepository).persist(any(Credential.class));

        // Act
        CredentialResponseDTO result = credentialService.registerCredential("test@example.com", requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("New SSH Key", result.tite());
        assertEquals("root", result.usename());
        assertNotNull(result.zrn());
        assertTrue(result.zrn().startsWith("zrn:zekret:credential:"));

        verify(userRepository, times(1)).findByEmailOrUsername("test@example.com", "test@example.com");
        verify(credentialTypeRepository, times(1)).findByZrn("ssh_credential");
        verify(namespaceRepository, times(1)).findByZrnAndUserId(testNamespace.getZrn(), 1L);
        verify(credentialRepository, times(1)).persist(any(Credential.class));
    }

    @Test
    void testRegisterCredential_UserNotFound_ThrowsException() {
        // Arrange
        CredentialRequestDTO requestDTO = new CredentialRequestDTO(
            "Test", "user", "pass", null, null, null, null, null, null, 
            "ssh_credential", testNamespace.getZrn()
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> credentialService.registerCredential("nonexistent@example.com", requestDTO)
        );

        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
        verify(credentialRepository, never()).persist(any(Credential.class));
    }

    @Test
    void testRegisterCredential_CredentialTypeNotFound_ThrowsException() {
        // Arrange
        CredentialRequestDTO requestDTO = new CredentialRequestDTO(
            "Test", "user", "pass", null, null, null, null, null, null, 
            "nonexistent_type", testNamespace.getZrn()
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialTypeRepository.findByZrn(anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> credentialService.registerCredential("test@example.com", requestDTO)
        );

        assertTrue(exception.getMessage().contains("nonexistent_type"));
        verify(credentialRepository, never()).persist(any(Credential.class));
    }

    @Test
    void testRegisterCredential_NamespaceNotFound_ThrowsException() {
        // Arrange
        String nonExistentNsZrn = "zrn:zekret:namespace:20250715:nonexistent";
        CredentialRequestDTO requestDTO = new CredentialRequestDTO(
            "Test", "user", "pass", null, null, null, null, null, null, 
            "ssh_credential", nonExistentNsZrn
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialTypeRepository.findByZrn(anyString()))
            .thenReturn(Optional.of(testCredentialType));
        when(namespaceRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> credentialService.registerCredential("test@example.com", requestDTO)
        );

        assertTrue(exception.getMessage().contains(nonExistentNsZrn));
        verify(credentialRepository, never()).persist(any(Credential.class));
    }

    @Test
    void testUpdateCredential_Success() {
        // Arrange
        CredentialRequestDTO updateDTO = new CredentialRequestDTO(
            "Updated SSH Key",
            "newadmin",
            "newpassword",
            null,
            null,
            null,
            null,
            null,
            "Updated notes",
            "ssh_credential",
            testNamespace.getZrn()
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(testCredential));
        when(credentialTypeRepository.findByZrn(anyString()))
            .thenReturn(Optional.of(testCredentialType));
        when(namespaceRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(testNamespace));

        // Act
        CredentialResponseDTO result = credentialService.updateCredential(
            "test@example.com", 
            testCredential.getZrn(), 
            updateDTO
        );

        // Assert
        assertNotNull(result);
        assertEquals("Updated SSH Key", result.tite());
        assertEquals("newadmin", result.usename());
        assertEquals("newpassword", result.pasword());
        assertEquals("Updated notes", result.notes());

        verify(credentialRepository, times(1)).findByZrnAndUserId(testCredential.getZrn(), 1L);
    }

    @Test
    void testUpdateCredential_UserNotFound_ThrowsException() {
        // Arrange
        CredentialRequestDTO updateDTO = new CredentialRequestDTO(
            "Updated", "user", "pass", null, null, null, null, null, null, 
            "ssh_credential", testNamespace.getZrn()
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> credentialService.updateCredential("nonexistent@example.com", "some-zrn", updateDTO)
        );

        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
        verify(credentialRepository, never()).findByZrnAndUserId(anyString(), anyLong());
    }

    @Test
    void testUpdateCredential_CredentialNotFound_ThrowsException() {
        // Arrange
        String nonExistentZrn = "zrn:zekret:credential:20250715:nonexistent";
        CredentialRequestDTO updateDTO = new CredentialRequestDTO(
            "Updated", "user", "pass", null, null, null, null, null, null, 
            "ssh_credential", testNamespace.getZrn()
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> credentialService.updateCredential("test@example.com", nonExistentZrn, updateDTO)
        );

        assertTrue(exception.getMessage().contains(nonExistentZrn));
    }

    @Test
    void testDeleteCredentialByZrnAndUserEmail_Success() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(testCredential));
        doNothing().when(credentialRepository).delete(any(Credential.class));

        // Act
        assertDoesNotThrow(() -> 
            credentialService.deleteCredentialByZrnAndUserEmail(testCredential.getZrn(), "test@example.com")
        );

        // Assert
        verify(userRepository, times(1)).findByEmailOrUsername("test@example.com", "test@example.com");
        verify(credentialRepository, times(1)).findByZrnAndUserId(testCredential.getZrn(), 1L);
        verify(credentialRepository, times(1)).delete(testCredential);
    }

    @Test
    void testDeleteCredentialByZrnAndUserEmail_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> credentialService.deleteCredentialByZrnAndUserEmail("some-zrn", "nonexistent@example.com")
        );

        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
        verify(credentialRepository, never()).delete(any(Credential.class));
    }

    @Test
    void testDeleteCredentialByZrnAndUserEmail_CredentialNotFound_ThrowsException() {
        // Arrange
        String nonExistentZrn = "zrn:zekret:credential:20250715:nonexistent";

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> credentialService.deleteCredentialByZrnAndUserEmail(nonExistentZrn, "test@example.com")
        );

        assertTrue(exception.getMessage().contains(nonExistentZrn));
        verify(credentialRepository, never()).delete(any(Credential.class));
    }

    @Test
    void testRegisterCredential_AssignsCorrectRelationships() {
        // Arrange
        CredentialRequestDTO requestDTO = new CredentialRequestDTO(
            "Test Credential", "user", "pass", null, null, null, null, null, null, 
            "ssh_credential", testNamespace.getZrn()
        );

        when(userRepository.findByEmailOrUsername(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(credentialTypeRepository.findByZrn(anyString()))
            .thenReturn(Optional.of(testCredentialType));
        when(namespaceRepository.findByZrnAndUserId(anyString(), anyLong()))
            .thenReturn(Optional.of(testNamespace));

        ArgumentCaptor<Credential> credentialCaptor = ArgumentCaptor.forClass(Credential.class);
        doNothing().when(credentialRepository).persist(credentialCaptor.capture());

        // Act
        credentialService.registerCredential("test@example.com", requestDTO);

        // Assert
        Credential capturedCredential = credentialCaptor.getValue();
        assertNotNull(capturedCredential);
        assertEquals(testUser, capturedCredential.getUser());
        assertEquals(testNamespace, capturedCredential.getNamespace());
        assertEquals(testCredentialType, capturedCredential.getCredentialType());
        assertNotNull(capturedCredential.getZrn());
    }
}
