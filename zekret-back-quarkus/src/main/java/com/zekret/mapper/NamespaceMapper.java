package com.zekret.mapper;

import com.zekret.dto.NamespaceRequestDTO;
import com.zekret.dto.NamespaceResponseDTO;
import com.zekret.model.Namespace;

public class NamespaceMapper {
    
    public static Namespace toEntity(NamespaceRequestDTO dto) {
        Namespace namespace = new Namespace();
        namespace.setName(dto.name());
        namespace.setDescription(dto.description());
        return namespace;
    }

    public static NamespaceResponseDTO toDTO(Namespace entity) {
        return new NamespaceResponseDTO(
            entity.getName(),
            entity.getZrn(),
            entity.getDescription(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCredentials() != null 
                ? entity.getCredentials().stream()
                    .map(credential -> CredentialMapper.toDTO(credential))
                    .toList()
                : null
        );
    }
}