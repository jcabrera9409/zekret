package com.zekret.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zekret.dto.APIResponseDTO;
import com.zekret.model.Namespace;
import com.zekret.model.User;
import com.zekret.service.INamespaceService;
import com.zekret.util.AuthenticationUtils;
import com.zekret.util.ZrnGenerator;

@RestController
@RequestMapping("/v1/namespaces")
public class NamespaceController {
    
    private static final Logger logger = LoggerFactory.getLogger(NamespaceController.class);
    
    @Autowired
    private INamespaceService namespaceService;
    
    @Autowired
    private AuthenticationUtils authenticationUtils;
    
    /**
     * Create a new namespace
     */
    @PostMapping
    public ResponseEntity<APIResponseDTO<Namespace>> createNamespace(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Namespace request) {
        try {
            User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
            logger.info("Creating namespace '{}' for user: {}", request.getName(), authenticatedUser.getUsername());
            
            // Configure the namespace
            request.setUser(authenticatedUser);
            request.setZrn(ZrnGenerator.generateNamespaceZrn());
            
            Namespace savedNamespace = namespaceService.register(request);
            
            APIResponseDTO<Namespace> response = APIResponseDTO.success(
                "Namespace created successfully", 
                savedNamespace, 
                HttpStatus.CREATED.value()
            );
            
            logger.info("Namespace created successfully: {}", savedNamespace.getZrn());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error creating namespace: {}", e.getMessage());
            APIResponseDTO<Namespace> response = APIResponseDTO.error(
                "Failed to create namespace: " + e.getMessage(), 
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Update an existing namespace
     */
    @PutMapping("/{zrn}")
    public ResponseEntity<APIResponseDTO<Namespace>> updateNamespace(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String zrn, 
            @RequestBody Namespace request) {
        try {
            User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
            logger.info("Updating namespace '{}' for user: {}", zrn, authenticatedUser.getUsername());
            
            // Find existing namespace using optimized query
            Optional<Namespace> existingNamespaceOpt = namespaceService.getNamespaceByZrnAndUserId(zrn, authenticatedUser.getId());
            
            if (!existingNamespaceOpt.isPresent()) {
                logger.warn("Namespace not found or access denied: {}", zrn);
                APIResponseDTO<Namespace> response = APIResponseDTO.error(
                    "Namespace not found or access denied", 
                    HttpStatus.NOT_FOUND.value()
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Namespace existingNamespace = existingNamespaceOpt.get();
            
            // Update allowed fields
            existingNamespace.setName(request.getName());
            existingNamespace.setDescription(request.getDescription());
            
            Namespace updatedNamespace = namespaceService.modify(existingNamespace);
            
            APIResponseDTO<Namespace> response = APIResponseDTO.success(
                "Namespace updated successfully", 
                updatedNamespace, 
                HttpStatus.OK.value()
            );
            
            logger.info("Namespace updated successfully: {}", updatedNamespace.getZrn());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating namespace: {}", e.getMessage());
            APIResponseDTO<Namespace> response = APIResponseDTO.error(
                "Failed to update namespace: " + e.getMessage(), 
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Get namespace by ZRN
     */
    @GetMapping("/{zrn}")
    public ResponseEntity<APIResponseDTO<Namespace>> getNamespaceByZrn(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String zrn) {
        try {
            User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
            logger.info("Finding namespace '{}' for user: {}", zrn, authenticatedUser.getUsername());
            
            // Use optimized query to find namespace by ZRN and user ID
            Optional<Namespace> namespaceOpt = namespaceService.getNamespaceByZrnAndUserId(zrn, authenticatedUser.getId());
            
            if (namespaceOpt.isPresent()) {
                APIResponseDTO<Namespace> response = APIResponseDTO.success(
                    "Namespace found", 
                    namespaceOpt.get(), 
                    HttpStatus.OK.value()
                );
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Namespace not found: {}", zrn);
                APIResponseDTO<Namespace> response = APIResponseDTO.error(
                    "Namespace not found", 
                    HttpStatus.NOT_FOUND.value()
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error finding namespace: {}", e.getMessage());
            APIResponseDTO<Namespace> response = APIResponseDTO.error(
                "Failed to find namespace: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all namespaces for the authenticated user
     */
    @GetMapping
    public ResponseEntity<APIResponseDTO<List<Namespace>>> getAllNamespaces(
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
            logger.info("Listing all namespaces for user: {}", authenticatedUser.getUsername());
            
            // Use optimized query to get namespaces by user ID
            List<Namespace> userNamespaces = namespaceService.getNamespacesByUserId(authenticatedUser.getId());
            
            APIResponseDTO<List<Namespace>> response = APIResponseDTO.success(
                "Namespaces retrieved successfully", 
                userNamespaces, 
                HttpStatus.OK.value()
            );
            
            logger.info("Found {} namespaces for user", userNamespaces.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error listing namespaces: {}", e.getMessage());
            APIResponseDTO<List<Namespace>> response = APIResponseDTO.error(
                "Failed to retrieve namespaces: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete a namespace physically
     */
    @DeleteMapping("/{zrn}")
    public ResponseEntity<APIResponseDTO<String>> deleteNamespace(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String zrn) {
        try {
            User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
            logger.info("Deleting namespace '{}' for user: {}", zrn, authenticatedUser.getUsername());
            
            // Use optimized query to find namespace by ZRN and user ID
            Optional<Namespace> namespaceOpt = namespaceService.getNamespaceByZrnAndUserId(zrn, authenticatedUser.getId());
            
            if (!namespaceOpt.isPresent()) {
                logger.warn("Namespace not found for deletion: {}", zrn);
                APIResponseDTO<String> response = APIResponseDTO.error(
                    "Namespace not found or access denied", 
                    HttpStatus.NOT_FOUND.value()
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Namespace namespaceToDelete = namespaceOpt.get();
            namespaceService.delete(namespaceToDelete.getId());
            
            APIResponseDTO<String> response = APIResponseDTO.success(
                "Namespace deleted successfully", 
                "Namespace '" + zrn + "' has been permanently deleted", 
                HttpStatus.OK.value()
            );
            
            logger.info("Namespace deleted successfully: {}", zrn);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting namespace: {}", e.getMessage());
            APIResponseDTO<String> response = APIResponseDTO.error(
                "Failed to delete namespace: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
