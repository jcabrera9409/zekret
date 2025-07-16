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
import com.zekret.model.Credential;
import com.zekret.model.Namespace;
import com.zekret.model.User;
import com.zekret.service.ICredentialService;
import com.zekret.service.INamespaceService;
import com.zekret.util.AuthenticationUtils;
import com.zekret.util.ZrnGenerator;

@RestController
@RequestMapping("/v1/credentials")
public class CredentialController {
    
    private static final Logger logger = LoggerFactory.getLogger(CredentialController.class);
    
    @Autowired
    private ICredentialService credentialService;
    
    @Autowired
    private INamespaceService namespaceService;
    
    @Autowired
    private AuthenticationUtils authenticationUtils;
    
    /**
     * Create a new credential
     * Note: Frontend should provide credential type as ZRN in the credentialType.zrn field
     */
    @PostMapping
    public ResponseEntity<APIResponseDTO<Credential>> createCredential(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Credential request) {
        try {
            User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
            logger.info("Creating credential '{}' for user: {}", request.getTitle(), authenticatedUser.getUsername());
            
            // The namespace ZRN should be provided in the request via the namespace object
            if (request.getNamespace() == null || request.getNamespace().getZrn() == null) {
                logger.warn("Namespace ZRN is required for credential creation");
                APIResponseDTO<Credential> response = APIResponseDTO.error(
                    "Namespace ZRN is required", 
                    HttpStatus.BAD_REQUEST.value()
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // The credential type ZRN should be provided in the request via the credentialType object
            if (request.getCredentialType() == null || request.getCredentialType().getZrn() == null) {
                logger.warn("Credential type ZRN is required for credential creation");
                APIResponseDTO<Credential> response = APIResponseDTO.error(
                    "Credential type ZRN is required", 
                    HttpStatus.BAD_REQUEST.value()
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Validate namespace exists and belongs to user
            Optional<Namespace> namespaceOpt = namespaceService.getNamespaceByZrnAndUserId(
                request.getNamespace().getZrn(), 
                authenticatedUser.getId()
            );
            
            if (!namespaceOpt.isPresent()) {
                logger.warn("Namespace not found or access denied: {}", request.getNamespace().getZrn());
                APIResponseDTO<Credential> response = APIResponseDTO.error(
                    "Namespace not found or access denied", 
                    HttpStatus.BAD_REQUEST.value()
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Set system-generated fields
            request.setUser(authenticatedUser);
            request.setNamespace(namespaceOpt.get());
            request.setZrn(ZrnGenerator.generateCredentialZrn());
            
            Credential savedCredential = credentialService.register(request);
            
            APIResponseDTO<Credential> response = APIResponseDTO.success(
                "Credential created successfully", 
                savedCredential, 
                HttpStatus.CREATED.value()
            );
            
            logger.info("Credential created successfully: {}", savedCredential.getZrn());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error creating credential: {}", e.getMessage());
            APIResponseDTO<Credential> response = APIResponseDTO.error(
                "Failed to create credential: " + e.getMessage(), 
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Update an existing credential
     * Note: Namespace assignment cannot be changed once set
     * Note: Credential type can be updated via credentialType.zrn field
     */
    @PutMapping("/{zrn}")
    public ResponseEntity<APIResponseDTO<Credential>> updateCredential(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String zrn, 
            @RequestBody Credential request) {
        try {
            User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
            logger.info("Updating credential '{}' for user: {}", zrn, authenticatedUser.getUsername());
            
            // Find existing credential using optimized query
            Optional<Credential> existingCredentialOpt = credentialService.getCredentialByZrnAndUserId(zrn, authenticatedUser.getId());
            
            if (!existingCredentialOpt.isPresent()) {
                logger.warn("Credential not found or access denied: {}", zrn);
                APIResponseDTO<Credential> response = APIResponseDTO.error(
                    "Credential not found or access denied", 
                    HttpStatus.NOT_FOUND.value()
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Credential existingCredential = existingCredentialOpt.get();
            
            // Update allowed fields (namespace and system fields cannot be changed)
            existingCredential.setTitle(request.getTitle());
            existingCredential.setUsername(request.getUsername());
            existingCredential.setPassword(request.getPassword());
            existingCredential.setSshPrivateKey(request.getSshPrivateKey());
            existingCredential.setSecretText(request.getSecretText());
            existingCredential.setFileName(request.getFileName());
            existingCredential.setFileContent(request.getFileContent());
            existingCredential.setNotes(request.getNotes());
            
            // Update credential type if provided (via ZRN)
            if (request.getCredentialType() != null && request.getCredentialType().getZrn() != null) {
                existingCredential.setCredentialType(request.getCredentialType());
            }
            
            Credential updatedCredential = credentialService.modify(existingCredential);
            
            APIResponseDTO<Credential> response = APIResponseDTO.success(
                "Credential updated successfully", 
                updatedCredential, 
                HttpStatus.OK.value()
            );
            
            logger.info("Credential updated successfully: {}", updatedCredential.getZrn());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating credential: {}", e.getMessage());
            APIResponseDTO<Credential> response = APIResponseDTO.error(
                "Failed to update credential: " + e.getMessage(), 
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Get credential by ZRN
     */
    @GetMapping("/{zrn}")
    public ResponseEntity<APIResponseDTO<Credential>> getCredentialByZrn(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String zrn) {
        try {
            User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
            logger.info("Finding credential '{}' for user: {}", zrn, authenticatedUser.getUsername());
            
            // Use optimized query to find credential by ZRN and user ID
            Optional<Credential> credentialOpt = credentialService.getCredentialByZrnAndUserId(zrn, authenticatedUser.getId());
            
            if (credentialOpt.isPresent()) {
                APIResponseDTO<Credential> response = APIResponseDTO.success(
                    "Credential found", 
                    credentialOpt.get(), 
                    HttpStatus.OK.value()
                );
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Credential not found: {}", zrn);
                APIResponseDTO<Credential> response = APIResponseDTO.error(
                    "Credential not found", 
                    HttpStatus.NOT_FOUND.value()
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error finding credential: {}", e.getMessage());
            APIResponseDTO<Credential> response = APIResponseDTO.error(
                "Failed to find credential: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all credentials for the authenticated user
     */
    @GetMapping
    public ResponseEntity<APIResponseDTO<List<Credential>>> getAllCredentials(
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
            logger.info("Listing all credentials for user: {}", authenticatedUser.getUsername());
            
            // Use optimized query to get credentials by user ID
            List<Credential> userCredentials = credentialService.getCredentialsByUserId(authenticatedUser.getId());
            
            APIResponseDTO<List<Credential>> response = APIResponseDTO.success(
                "Credentials retrieved successfully", 
                userCredentials, 
                HttpStatus.OK.value()
            );
            
            logger.info("Found {} credentials for user", userCredentials.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error listing credentials: {}", e.getMessage());
            APIResponseDTO<List<Credential>> response = APIResponseDTO.error(
                "Failed to retrieve credentials: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete a credential physically
     */
    @DeleteMapping("/{zrn}")
    public ResponseEntity<APIResponseDTO<String>> deleteCredential(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String zrn) {
        try {
            User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
            logger.info("Deleting credential '{}' for user: {}", zrn, authenticatedUser.getUsername());
            
            // Use optimized query to find credential by ZRN and user ID
            Optional<Credential> credentialOpt = credentialService.getCredentialByZrnAndUserId(zrn, authenticatedUser.getId());
            
            if (!credentialOpt.isPresent()) {
                logger.warn("Credential not found for deletion: {}", zrn);
                APIResponseDTO<String> response = APIResponseDTO.error(
                    "Credential not found or access denied", 
                    HttpStatus.NOT_FOUND.value()
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Credential credentialToDelete = credentialOpt.get();
            credentialService.delete(credentialToDelete.getId());
            
            APIResponseDTO<String> response = APIResponseDTO.success(
                "Credential deleted successfully", 
                "Credential '" + zrn + "' has been permanently deleted", 
                HttpStatus.OK.value()
            );
            
            logger.info("Credential deleted successfully: {}", zrn);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting credential: {}", e.getMessage());
            APIResponseDTO<String> response = APIResponseDTO.error(
                "Failed to delete credential: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all credentials for a specific namespace
     */
    @GetMapping("/namespace/{namespaceZrn}")
    public ResponseEntity<APIResponseDTO<List<Credential>>> getCredentialsByNamespace(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String namespaceZrn) {
        try {
            User authenticatedUser = authenticationUtils.getAuthenticatedUserFromToken(authorizationHeader);
            logger.info("Listing credentials for namespace '{}' and user: {}", namespaceZrn, authenticatedUser.getUsername());
            
            // Validate namespace exists and belongs to user
            if (!namespaceService.existsNamespaceByZrnAndUserId(namespaceZrn, authenticatedUser.getId())) {
                logger.warn("Namespace not found or access denied: {}", namespaceZrn);
                APIResponseDTO<List<Credential>> response = APIResponseDTO.error(
                    "Namespace not found or access denied", 
                    HttpStatus.NOT_FOUND.value()
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Use optimized query to get credentials by namespace and user ID
            List<Credential> namespaceCredentials = credentialService.getCredentialsByNamespaceAndUserId(
                namespaceZrn, 
                authenticatedUser.getId()
            );
            
            APIResponseDTO<List<Credential>> response = APIResponseDTO.success(
                "Credentials retrieved successfully for namespace", 
                namespaceCredentials, 
                HttpStatus.OK.value()
            );
            
            logger.info("Found {} credentials for namespace '{}' and user", namespaceCredentials.size(), namespaceZrn);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error listing credentials for namespace: {}", e.getMessage());
            APIResponseDTO<List<Credential>> response = APIResponseDTO.error(
                "Failed to retrieve credentials for namespace: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
