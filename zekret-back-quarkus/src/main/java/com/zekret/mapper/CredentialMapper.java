package com.zekret.mapper;

import com.zekret.dto.CredentialRequestDTO;
import com.zekret.dto.CredentialResponseDTO;
import com.zekret.dto.CredentialTypeResponseDTO;
import com.zekret.dto.NamespaceResponseDTO;
import com.zekret.model.Credential;
import com.zekret.model.CredentialType;
import com.zekret.model.Namespace;

public class CredentialMapper {

    public static Credential toEntity(CredentialRequestDTO dto) {
        Credential credential = new Credential();
        credential.setTitle(dto.title());
        credential.setUsername(dto.username());
        credential.setPassword(dto.password());
        credential.setSshPublicKey(dto.sshPublicKey());
        credential.setSshPrivateKey(dto.sshPrivateKey());
        credential.setSecretText(dto.secretText());
        credential.setFileName(dto.fileName());
        credential.setFileContent(dto.fileContent());
        credential.setNotes(dto.notes());

        CredentialType credentialType = new CredentialType();
        credentialType.setZrn(dto.credentialTypeZrn());
        credential.setCredentialType(credentialType);

        Namespace namespace = new Namespace();
        namespace.setZrn(dto.namespaceZrn());
        credential.setNamespace(namespace);

        return credential;
    }

    public static CredentialResponseDTO toDTO(Credential credential) {
        CredentialTypeResponseDTO typeDTO = new CredentialTypeResponseDTO(
            credential.getCredentialType().getZrn(),
            credential.getCredentialType().getName()
        );

        NamespaceResponseDTO namespaceDTO = new NamespaceResponseDTO (
            credential.getNamespace().getName(),
            credential.getNamespace().getZrn(),
            credential.getNamespace().getDescription(),
            credential.getNamespace().getCreatedAt(),
            credential.getNamespace().getUpdatedAt(),
            null
        );

        CredentialResponseDTO dto = new CredentialResponseDTO (
            credential.getZrn(),
            credential.getTitle(),
            credential.getUsername(),
            credential.getPassword(),
            credential.getSshPublicKey(),
            credential.getSshPrivateKey(),
            credential.getSecretText(),
            credential.getFileName(),
            credential.getFileContent(),
            credential.getNotes(),
            credential.getCreatedAt(),
            credential.getUpdatedAt(),
            typeDTO,
            namespaceDTO
        );

        return dto;
    }
}