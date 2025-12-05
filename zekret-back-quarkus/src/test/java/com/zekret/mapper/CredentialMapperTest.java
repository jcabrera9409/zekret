package com.zekret.mapper;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.zekret.dto.CredentialRequestDTO;
import com.zekret.dto.CredentialResponseDTO;
import com.zekret.dto.CredentialTypeResponseDTO;
import com.zekret.dto.NamespaceResponseDTO;
import com.zekret.model.Credential;
import com.zekret.model.CredentialType;
import com.zekret.model.Namespace;

class CredentialMapperTest {

    @Test
    void testToEntity_ValidDTO_AllFields() {
        CredentialRequestDTO dto = new CredentialRequestDTO(
            "Production SSH Key",
            "admin",
            "password123",
            "ssh-rsa AAAAB3...",
            "-----BEGIN RSA PRIVATE KEY-----...",
            "secret123",
            "config.json",
            "{\"key\": \"value\"}",
            "Important notes",
            "ssh_credential",
            "zrn:zekret:namespace:20250715:ns-123"
        );

        Credential credential = CredentialMapper.toEntity(dto);

        assertNotNull(credential);
        assertEquals(dto.title(), credential.getTitle());
        assertEquals(dto.username(), credential.getUsername());
        assertEquals(dto.password(), credential.getPassword());
        assertEquals(dto.sshPublicKey(), credential.getSshPublicKey());
        assertEquals(dto.sshPrivateKey(), credential.getSshPrivateKey());
        assertEquals(dto.secretText(), credential.getSecretText());
        assertEquals(dto.fileName(), credential.getFileName());
        assertEquals(dto.fileContent(), credential.getFileContent());
        assertEquals(dto.notes(), credential.getNotes());
        assertNotNull(credential.getCredentialType());
        assertEquals(dto.credentialTypeZrn(), credential.getCredentialType().getZrn());
        assertNotNull(credential.getNamespace());
        assertEquals(dto.namespaceZrn(), credential.getNamespace().getZrn());
    }

    @Test
    void testToEntity_MinimalFields() {
        CredentialRequestDTO dto = new CredentialRequestDTO(
            "Simple Credential",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "password_manager",
            "zrn:zekret:namespace:20250715:ns-456"
        );

        Credential credential = CredentialMapper.toEntity(dto);

        assertNotNull(credential);
        assertEquals("Simple Credential", credential.getTitle());
        assertNull(credential.getUsername());
        assertNull(credential.getPassword());
        assertNull(credential.getSshPublicKey());
        assertNull(credential.getSshPrivateKey());
        assertNull(credential.getSecretText());
        assertNull(credential.getFileName());
        assertNull(credential.getFileContent());
        assertNull(credential.getNotes());
        assertEquals("password_manager", credential.getCredentialType().getZrn());
        assertEquals("zrn:zekret:namespace:20250715:ns-456", credential.getNamespace().getZrn());
    }

    @Test
    void testToDTO_CompleteCredential() {
        // Create credential type
        CredentialType credType = new CredentialType();
        credType.setZrn("ssh_credential");
        credType.setName("SSH Credential");

        // Create namespace
        Namespace namespace = new Namespace();
        namespace.setName("Production");
        namespace.setZrn("zrn:zekret:namespace:20250715:prod-789");
        namespace.setDescription("Production namespace");
        namespace.prePersist();
        namespace.setCredentials(new ArrayList<>());

        // Create credential
        Credential credential = new Credential();
        credential.setTitle("SSH Key Production");
        credential.setZrn("zrn:zekret:credential:20250715:cred-111");
        credential.setUsername("root");
        credential.setPassword("rootpass");
        credential.setSshPublicKey("ssh-rsa AAAAB3...");
        credential.setSshPrivateKey("-----BEGIN RSA PRIVATE KEY-----...");
        credential.setSecretText("secret");
        credential.setFileName("config.yml");
        credential.setFileContent("config: value");
        credential.setNotes("Important server");
        credential.setCredentialType(credType);
        credential.setNamespace(namespace);
        credential.prePersist();

        CredentialResponseDTO dto = CredentialMapper.toDTO(credential);

        assertNotNull(dto);
        assertEquals(credential.getZrn(), dto.zrn());
        assertEquals(credential.getTitle(), dto.tite());
        assertEquals(credential.getUsername(), dto.usename());
        assertEquals(credential.getPassword(), dto.pasword());
        assertEquals(credential.getSshPublicKey(), dto.sshublicKey());
        assertEquals(credential.getSshPrivateKey(), dto.sshrivateKey());
        assertEquals(credential.getSecretText(), dto.secetText());
        assertEquals(credential.getFileName(), dto.filName());
        assertEquals(credential.getFileContent(), dto.filContent());
        assertEquals(credential.getNotes(), dto.notes());
        assertNotNull(dto.createdAt());
        assertNotNull(dto.updatedAt());
        
        // Verify nested DTOs
        assertNotNull(dto.credentialType());
        assertEquals("ssh_credential", dto.credentialType().zrn());
        assertEquals("SSH Credential", dto.credentialType().name());
        
        assertNotNull(dto.namespace());
        assertEquals("Production", dto.namespace().name());
        assertEquals("zrn:zekret:namespace:20250715:prod-789", dto.namespace().zrn());
    }

    @Test
    void testToDTO_MinimalCredential() {
        // Create credential type
        CredentialType credType = new CredentialType();
        credType.setZrn("password_manager");
        credType.setName("Password Manager");

        // Create namespace
        Namespace namespace = new Namespace();
        namespace.setName("Development");
        namespace.setZrn("zrn:zekret:namespace:20250715:dev-222");
        namespace.setDescription("Dev namespace");
        namespace.prePersist();
        namespace.setCredentials(new ArrayList<>());

        // Create minimal credential
        Credential credential = new Credential();
        credential.setTitle("Basic Password");
        credential.setZrn("zrn:zekret:credential:20250715:cred-333");
        credential.setCredentialType(credType);
        credential.setNamespace(namespace);
        credential.prePersist();

        CredentialResponseDTO dto = CredentialMapper.toDTO(credential);

        assertNotNull(dto);
        assertEquals("Basic Password", dto.tite());
        assertEquals("zrn:zekret:credential:20250715:cred-333", dto.zrn());
        assertNull(dto.usename());
        assertNull(dto.pasword());
        assertNull(dto.sshublicKey());
        assertNull(dto.sshrivateKey());
        assertNotNull(dto.credentialType());
        assertNotNull(dto.namespace());
    }

    @Test
    void testRoundTrip_RequestToEntityAndToResponseDTO() {
        // Create request DTO
        CredentialRequestDTO requestDTO = new CredentialRequestDTO(
            "Test Credential",
            "testuser",
            "testpass",
            null,
            null,
            null,
            null,
            null,
            "Test notes",
            "password_manager",
            "zrn:zekret:namespace:20250715:ns-test"
        );

        // Convert to entity
        Credential credential = CredentialMapper.toEntity(requestDTO);
        credential.setZrn("zrn:zekret:credential:20250715:cred-test");
        credential.prePersist();
        
        // Set up full credential type
        CredentialType credType = new CredentialType();
        credType.setZrn("password_manager");
        credType.setName("Password Manager");
        credential.setCredentialType(credType);
        
        // Set up full namespace
        Namespace namespace = new Namespace();
        namespace.setName("Test NS");
        namespace.setZrn("zrn:zekret:namespace:20250715:ns-test");
        namespace.setDescription("Test");
        namespace.prePersist();
        namespace.setCredentials(new ArrayList<>());
        credential.setNamespace(namespace);

        // Convert to response DTO
        CredentialResponseDTO responseDTO = CredentialMapper.toDTO(credential);

        // Verify data integrity
        assertEquals(requestDTO.title(), responseDTO.tite());
        assertEquals(requestDTO.username(), responseDTO.usename());
        assertEquals(requestDTO.password(), responseDTO.pasword());
        assertEquals(requestDTO.notes(), responseDTO.notes());
        assertNotNull(responseDTO.zrn());
        assertNotNull(responseDTO.createdAt());
    }

    @Test
    void testToEntity_SSHCredential() {
        CredentialRequestDTO dto = new CredentialRequestDTO(
            "GitHub SSH Key",
            "git",
            null,
            "ssh-rsa AAAAB3NzaC1yc2E...",
            "-----BEGIN OPENSSH PRIVATE KEY-----\nkey content\n-----END OPENSSH PRIVATE KEY-----",
            null,
            null,
            null,
            "GitHub access key",
            "ssh_credential",
            "zrn:zekret:namespace:20250715:github-ns"
        );

        Credential credential = CredentialMapper.toEntity(dto);

        assertNotNull(credential);
        assertEquals("GitHub SSH Key", credential.getTitle());
        assertEquals("git", credential.getUsername());
        assertNull(credential.getPassword());
        assertNotNull(credential.getSshPublicKey());
        assertNotNull(credential.getSshPrivateKey());
        assertTrue(credential.getSshPublicKey().startsWith("ssh-rsa"));
        assertTrue(credential.getSshPrivateKey().contains("PRIVATE KEY"));
    }

    @Test
    void testToEntity_FileCredential() {
        CredentialRequestDTO dto = new CredentialRequestDTO(
            "API Config File",
            null,
            null,
            null,
            null,
            null,
            "api-config.json",
            "{\"apiKey\": \"secret123\", \"endpoint\": \"https://api.example.com\"}",
            "Production API configuration",
            "secret_file",
            "zrn:zekret:namespace:20250715:api-ns"
        );

        Credential credential = CredentialMapper.toEntity(dto);

        assertNotNull(credential);
        assertEquals("API Config File", credential.getTitle());
        assertNull(credential.getUsername());
        assertNull(credential.getPassword());
        assertEquals("api-config.json", credential.getFileName());
        assertNotNull(credential.getFileContent());
        assertTrue(credential.getFileContent().contains("apiKey"));
    }

    @Test
    void testToDTO_NestedObjectsAreMapped() {
        CredentialType credType = new CredentialType();
        credType.setZrn("test_type");
        credType.setName("Test Type");

        Namespace namespace = new Namespace();
        namespace.setName("Test Namespace");
        namespace.setZrn("zrn:zekret:namespace:20250715:test-ns");
        namespace.setDescription("Test");
        namespace.prePersist();
        namespace.setCredentials(new ArrayList<>());

        Credential credential = new Credential();
        credential.setTitle("Test");
        credential.setZrn("zrn:zekret:credential:20250715:test");
        credential.setCredentialType(credType);
        credential.setNamespace(namespace);
        credential.prePersist();

        CredentialResponseDTO dto = CredentialMapper.toDTO(credential);

        assertNotNull(dto);
        
        // Verify credential type mapping
        CredentialTypeResponseDTO typeDTO = dto.credentialType();
        assertNotNull(typeDTO);
        assertEquals("test_type", typeDTO.zrn());
        assertEquals("Test Type", typeDTO.name());
        
        // Verify namespace mapping
        NamespaceResponseDTO nsDTO = dto.namespace();
        assertNotNull(nsDTO);
        assertEquals("Test Namespace", nsDTO.name());
        assertEquals("zrn:zekret:namespace:20250715:test-ns", nsDTO.zrn());
        assertNull(nsDTO.credentials()); // Should be null to avoid circular reference
    }
}
