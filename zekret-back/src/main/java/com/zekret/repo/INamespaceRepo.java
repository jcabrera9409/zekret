package com.zekret.repo;

import java.util.List;
import java.util.Optional;

import com.zekret.model.Namespace;

public interface INamespaceRepo extends IGenericRepo<Namespace, Long> {
    
    /**
     * Find all namespaces for a specific user
     * Spring Data JPA automatically generates: SELECT n FROM Namespace n WHERE n.user.id = :userId
     * 
     * @param userId The user ID to filter by
     * @return List of namespaces belonging to the user
     */
    List<Namespace> findByUserId(Long userId);
    
    /**
     * Find a namespace by ZRN and user ID
     * Spring Data JPA automatically generates: SELECT n FROM Namespace n WHERE n.zrn = :zrn AND n.user.id = :userId
     * 
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     * @return Optional containing the namespace if found
     */
    Optional<Namespace> findByZrnAndUserId(String zrn, Long userId);
    
    /**
     * Check if a namespace with the given ZRN exists for the user
     * Spring Data JPA automatically generates: SELECT COUNT(n) > 0 FROM Namespace n WHERE n.zrn = :zrn AND n.user.id = :userId
     * 
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     * @return true if the namespace exists, false otherwise
     */
    boolean existsByZrnAndUserId(String zrn, Long userId);
}
