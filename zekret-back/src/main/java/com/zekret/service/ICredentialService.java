package com.zekret.service;

import java.util.List;
import java.util.Optional;

import com.zekret.model.Credential;

public interface ICredentialService extends ICRUD<Credential, Long> {
    
    /**
     * Get all credentials for a specific user
     * 
     * @param userId The user ID to filter by
     * @return List of credentials belonging to the user
     */
    List<Credential> getCredentialsByUserId(Long userId);
    
    /**
     * Get a credential by ZRN and user ID
     * 
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     * @return Optional containing the credential if found
     */
    Optional<Credential> getCredentialByZrnAndUserId(String zrn, Long userId);
    
    /**
     * Get all credentials for a specific namespace and user
     * 
     * @param namespaceZrn The namespace ZRN
     * @param userId The user ID to filter by
     * @return List of credentials in the namespace belonging to the user
     */
    List<Credential> getCredentialsByNamespaceAndUserId(String namespaceZrn, Long userId);
    
    /**
     * Check if a credential exists for the user
     * 
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     * @return true if the credential exists, false otherwise
     */
    boolean existsCredentialByZrnAndUserId(String zrn, Long userId);
}
