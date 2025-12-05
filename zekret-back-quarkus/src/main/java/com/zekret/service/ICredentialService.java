package com.zekret.service;

import java.util.List;

import com.zekret.dto.CredentialRequestDTO;
import com.zekret.dto.CredentialResponseDTO;

public interface ICredentialService {

    /**
     * Get all credentials for the authenticated user and namespace ZRN.
     * 
     * @param namespaceZrn The namespace ZRN to filter credentials.
     * @param userEmail The user email to filter credentials.
     * @return List of credentials in the specified namespace for the user.
     */
    List<CredentialResponseDTO> getCredentialsByNamespaceAndUserId(String namespaceZrn, String userEmail);

    /**
     * Get a credential by ZRN and user email.
     * 
     * @param zrn The ZRN identifier of the credential.
     * @param userEmail The user email to filter by.
     * @return The credential if found, null otherwise.
     */
    CredentialResponseDTO getCredentialByZrnAndUserEmail(String zrn, String userEmail);
    
    /**
     * Register a new credential for a user.
     * 
     * @param userEmail The user email to associate the credential with.
     * @param credentialRequestDTO The credential data to register.
     * @return The registered CredentialResponseDTO.
     */
    CredentialResponseDTO registerCredential(String userEmail, CredentialRequestDTO credentialRequestDTO);

    /**
     * Update an existing credential for a user.
     * 
     * @param userEmail The user email to associate the credential with.
     * @param zrn The ZRN identifier of the credential to update.
     * @param credentialRequestDTO The credential data to update.
     * @return The updated CredentialResponseDTO.
     */
    CredentialResponseDTO updateCredential(String userEmail, String zrn, CredentialRequestDTO credentialRequestDTO);

    /**
     * Delete a credential by ZRN and user email.
     * 
     * @param zrn The ZRN identifier of the credential to delete.
     * @param userEmail The user email to filter by.
     */
    void deleteCredentialByZrnAndUserEmail(String zrn, String userEmail);
}