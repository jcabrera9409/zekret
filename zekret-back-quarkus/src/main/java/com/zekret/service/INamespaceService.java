package com.zekret.service;

import java.util.List;

import com.zekret.dto.NamespaceRequestDTO;
import com.zekret.dto.NamespaceResponseDTO;

public interface INamespaceService {
    
    /**
     * Get all namespaces for a specific user
     * @param userId The user ID to filter by
     * @return List of namespaces belonging to the user
     */
    List<NamespaceResponseDTO> getNamespacesByUserId(Long userId);

    /**
     * Get a namespace by ZRN and user ID
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     * @return NamespaceResponseDTO if found, null otherwise
     */
    NamespaceResponseDTO getNamespaceByZrnAndUserId(String zrn, Long userId);

    /**
     * Register a new namespace for a user
     * @param userId The user ID to associate the namespace with
     * @param namespace The namespace to register
     * @return The registered NamespaceResponseDTO
     */
    NamespaceResponseDTO registerNamespace(Long userId, NamespaceRequestDTO namespace);

    /**
     * Update an existing namespace for a user
     * @param userId The user ID to associate the namespace with
     * @param namespace The namespace to update
     * @return The updated NamespaceResponseDTO
     */
    NamespaceResponseDTO updateNamespace(Long userId, NamespaceRequestDTO namespace);

    /**
     * Delete a namespace by ZRN and user ID
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     */
    void deleteNamespaceByZrnAndUserId(String zrn, Long userId);
}
