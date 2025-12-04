package com.zekret.service;

import java.util.List;

import com.zekret.dto.NamespaceRequestDTO;
import com.zekret.dto.NamespaceResponseDTO;

public interface INamespaceService {
    
    /**
     * Get all namespaces for a specific user
     * @param userEmail The user email to filter by
     * @return List of namespaces belonging to the user
     */
    List<NamespaceResponseDTO> getNamespacesByUserEmail(String userEmail);

    /**
     * Get a namespace by ZRN and user email
     * @param zrn The ZRN identifier
     * @param userEmail The user email to filter by
     * @return NamespaceResponseDTO if found, null otherwise
     */
    NamespaceResponseDTO getNamespaceByZrnAndUserEmail(String zrn, String userEmail);

    /**
     * Register a new namespace for a user
     * @param userEmail The user email to associate the namespace with
     * @param namespace The namespace to register
     * @return The registered NamespaceResponseDTO
     */
    NamespaceResponseDTO registerNamespace(String userEmail, NamespaceRequestDTO namespace);
    /**
     * Update an existing namespace for a user
     * @param userEmail The user email to associate the namespace with
     * @param zrn The ZRN identifier of the namespace to update
     * @param namespace The namespace to update
     * @return The updated NamespaceResponseDTO
     */
    NamespaceResponseDTO updateNamespace(String userEmail, String zrn, NamespaceRequestDTO namespace);

    /**
     * Delete a namespace by ZRN and user email
     * @param zrn The ZRN identifier
     * @param userEmail The user email to filter by
     */
    void deleteNamespaceByZrnAndUserEmail(String zrn, String userEmail);
}
