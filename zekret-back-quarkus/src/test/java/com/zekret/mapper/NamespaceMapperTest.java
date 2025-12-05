package com.zekret.mapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.zekret.dto.CredentialResponseDTO;
import com.zekret.dto.NamespaceRequestDTO;
import com.zekret.dto.NamespaceResponseDTO;
import com.zekret.model.Credential;
import com.zekret.model.CredentialType;
import com.zekret.model.Namespace;

class NamespaceMapperTest {

    @Test
    void testToEntity_ValidDTO() {
        NamespaceRequestDTO dto = new NamespaceRequestDTO(
            "Production",
            "Production environment namespace"
        );

        Namespace namespace = NamespaceMapper.toEntity(dto);

        assertNotNull(namespace);
        assertEquals(dto.name(), namespace.getName());
        assertEquals(dto.description(), namespace.getDescription());
    }

    @Test
    void testToEntity_AllFieldsAreSet() {
        NamespaceRequestDTO dto = new NamespaceRequestDTO(
            "Development",
            "Development environment for testing"
        );

        Namespace namespace = NamespaceMapper.toEntity(dto);

        assertNotNull(namespace);
        assertNotNull(namespace.getName());
        assertNotNull(namespace.getDescription());
        assertFalse(namespace.getName().isEmpty());
        assertFalse(namespace.getDescription().isEmpty());
    }

    @Test
    void testToDTO_ValidNamespaceWithoutCredentials() {
        Namespace namespace = new Namespace();
        namespace.setName("Test Namespace");
        namespace.setZrn("zrn:zekret:namespace:20250715:test-123");
        namespace.setDescription("Test description");
        namespace.prePersist();
        namespace.setCredentials(new ArrayList<>());

        NamespaceResponseDTO dto = NamespaceMapper.toDTO(namespace);

        assertNotNull(dto);
        assertEquals(namespace.getName(), dto.name());
        assertEquals(namespace.getZrn(), dto.zrn());
        assertEquals(namespace.getDescription(), dto.description());
        assertNotNull(dto.createdAt());
        assertNotNull(dto.credentials());
        assertTrue(dto.credentials().isEmpty());
    }

    @Test
    void testToDTO_ValidNamespaceWithCredentials() {
        // Create namespace
        Namespace namespace = new Namespace();
        namespace.setName("Test Namespace");
        namespace.setZrn("zrn:zekret:namespace:20250715:test-123");
        namespace.setDescription("Test description");
        namespace.prePersist();

        // Create credential
        Credential credential = new Credential();
        credential.setTitle("Test Credential");
        credential.setZrn("zrn:zekret:credential:20250715:cred-123");
        credential.setUsername("testuser");
        credential.setPassword("testpass");
        credential.prePersist();
        credential.setNamespace(namespace);
        
        // Create credential type
        CredentialType credType = new CredentialType();
        credType.setZrn("ssh_credential");
        credType.setName("SSH Credential");
        credential.setCredentialType(credType);

        List<Credential> credentials = new ArrayList<>();
        credentials.add(credential);
        namespace.setCredentials(credentials);

        NamespaceResponseDTO dto = NamespaceMapper.toDTO(namespace);

        assertNotNull(dto);
        assertEquals(namespace.getName(), dto.name());
        assertEquals(namespace.getZrn(), dto.zrn());
        assertEquals(namespace.getDescription(), dto.description());
        assertNotNull(dto.credentials());
        assertEquals(1, dto.credentials().size());
        
        CredentialResponseDTO credDto = dto.credentials().get(0);
        assertEquals("Test Credential", credDto.tite());
        assertEquals("testuser", credDto.usename());
    }

    @Test
    void testToDTO_AllFieldsAreMapped() {
        Namespace namespace = new Namespace();
        namespace.setName("Complete Namespace");
        namespace.setZrn("zrn:zekret:namespace:20250715:complete-456");
        namespace.setDescription("Complete test description");
        namespace.prePersist();
        namespace.preUpdate();
        namespace.setCredentials(new ArrayList<>());

        NamespaceResponseDTO dto = NamespaceMapper.toDTO(namespace);

        assertNotNull(dto);
        assertNotNull(dto.name());
        assertNotNull(dto.zrn());
        assertNotNull(dto.description());
        assertNotNull(dto.createdAt());
        assertNotNull(dto.updatedAt());
        assertNotNull(dto.credentials());
    }

    @Test
    void testRoundTrip_RequestToEntityAndToResponseDTO() {
        // Create request DTO
        NamespaceRequestDTO requestDTO = new NamespaceRequestDTO(
            "Staging",
            "Staging environment"
        );

        // Convert to entity
        Namespace namespace = NamespaceMapper.toEntity(requestDTO);
        namespace.setZrn("zrn:zekret:namespace:20250715:staging-789");
        namespace.prePersist();
        namespace.setCredentials(new ArrayList<>());

        // Convert to response DTO
        NamespaceResponseDTO responseDTO = NamespaceMapper.toDTO(namespace);

        // Verify data integrity
        assertEquals(requestDTO.name(), responseDTO.name());
        assertEquals(requestDTO.description(), responseDTO.description());
        assertEquals("zrn:zekret:namespace:20250715:staging-789", responseDTO.zrn());
        assertNotNull(responseDTO.createdAt());
        assertNotNull(responseDTO.credentials());
    }

    @Test
    void testToEntity_PreservesExactValues() {
        String testName = "Exact Name";
        String testDescription = "Exact Description Value";
        
        NamespaceRequestDTO dto = new NamespaceRequestDTO(testName, testDescription);
        Namespace namespace = NamespaceMapper.toEntity(dto);

        assertEquals(testName, namespace.getName());
        assertEquals(testDescription, namespace.getDescription());
    }

    @Test
    void testToDTO_EmptyCredentialsList() {
        Namespace namespace = new Namespace();
        namespace.setName("Empty Namespace");
        namespace.setZrn("zrn:zekret:namespace:20250715:empty-000");
        namespace.setDescription("Namespace without credentials");
        namespace.prePersist();
        namespace.setCredentials(new ArrayList<>());

        NamespaceResponseDTO dto = NamespaceMapper.toDTO(namespace);

        assertNotNull(dto);
        assertNotNull(dto.credentials());
        assertEquals(0, dto.credentials().size());
    }

    @Test
    void testToDTO_MultipleCredentials() {
        // Create namespace
        Namespace namespace = new Namespace();
        namespace.setName("Multi Credential Namespace");
        namespace.setZrn("zrn:zekret:namespace:20250715:multi-999");
        namespace.setDescription("Namespace with multiple credentials");
        namespace.prePersist();

        // Create credential type
        CredentialType credType = new CredentialType();
        credType.setZrn("password_manager");
        credType.setName("Password Manager");

        // Create multiple credentials
        List<Credential> credentials = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Credential credential = new Credential();
            credential.setTitle("Credential " + i);
            credential.setZrn("zrn:zekret:credential:20250715:cred-" + i);
            credential.setUsername("user" + i);
            credential.setPassword("pass" + i);
            credential.prePersist();
            credential.setNamespace(namespace);
            credential.setCredentialType(credType);
            credentials.add(credential);
        }
        namespace.setCredentials(credentials);

        NamespaceResponseDTO dto = NamespaceMapper.toDTO(namespace);

        assertNotNull(dto);
        assertNotNull(dto.credentials());
        assertEquals(3, dto.credentials().size());
        
        // Verify each credential
        for (int i = 0; i < 3; i++) {
            CredentialResponseDTO credDto = dto.credentials().get(i);
            assertNotNull(credDto);
            assertEquals("Credential " + (i + 1), credDto.tite());
        }
    }
}
