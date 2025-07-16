package com.zekret.repo;

import java.util.List;
import java.util.Optional;

import com.zekret.model.Credential;

public interface ICredentialRepo extends IGenericRepo<Credential, Long> {
    
    /**
     * Find all credentials for a specific user
     * Spring Data JPA automatically generates: SELECT c FROM Credential c WHERE c.user.id = :userId
     * 
     * @param userId The user ID to filter by
     * @return List of credentials belonging to the user
     */
    List<Credential> findByUserId(Long userId);
    
    /**
     * Find a credential by ZRN and user ID
     * Spring Data JPA automatically generates: SELECT c FROM Credential c WHERE c.zrn = :zrn AND c.user.id = :userId
     * 
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     * @return Optional containing the credential if found
     */
    Optional<Credential> findByZrnAndUserId(String zrn, Long userId);
    
    /**
     * Find all credentials for a specific namespace and user
     * Spring Data JPA automatically generates: SELECT c FROM Credential c WHERE c.namespace.zrn = :namespaceZrn AND c.user.id = :userId
     * 
     * @param namespaceZrn The namespace ZRN
     * @param userId The user ID to filter by
     * @return List of credentials in the namespace belonging to the user
     */
    List<Credential> findByNamespaceZrnAndUserId(String namespaceZrn, Long userId);
    
    /**
     * Check if a credential with the given ZRN exists for the user
     * Spring Data JPA automatically generates: SELECT COUNT(c) > 0 FROM Credential c WHERE c.zrn = :zrn AND c.user.id = :userId
     * 
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     * @return true if the credential exists, false otherwise
     */
    boolean existsByZrnAndUserId(String zrn, Long userId);
}
