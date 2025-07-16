package com.zekret.service;

import java.util.List;
import java.util.Optional;

import com.zekret.model.Namespace;

public interface INamespaceService extends ICRUD<Namespace, Long> {
    
    /**
     * Get all namespaces for a specific user
     * 
     * @param userId The user ID to filter by
     * @return List of namespaces belonging to the user
     */
    List<Namespace> getNamespacesByUserId(Long userId);
    
    /**
     * Get a namespace by ZRN and user ID
     * 
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     * @return Optional containing the namespace if found
     */
    Optional<Namespace> getNamespaceByZrnAndUserId(String zrn, Long userId);
    
    /**
     * Check if a namespace exists for the user
     * 
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     * @return true if the namespace exists, false otherwise
     */
    boolean existsNamespaceByZrnAndUserId(String zrn, Long userId);
}
