package com.zekret.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zekret.model.Credential;
import com.zekret.model.CredentialType;
import com.zekret.model.Namespace;
import com.zekret.repo.ICredentialRepo;
import com.zekret.repo.ICredentialTypeRepo;
import com.zekret.repo.IGenericRepo;
import com.zekret.repo.INamespaceRepo;
import com.zekret.service.ICredentialService;

@Service
public class CredentialServiceImpl extends CRUDImpl<Credential, Long> implements ICredentialService {

    private static final Logger logger = LoggerFactory.getLogger(CredentialServiceImpl.class);

    private final ICredentialRepo credentialRepo;
    
    @Autowired
    private INamespaceRepo namespaceRepo;
    
    @Autowired
    private ICredentialTypeRepo credentialTypeRepo;

    public CredentialServiceImpl(ICredentialRepo credentialRepo) {
        this.credentialRepo = credentialRepo;
    }

    @Override
    public Credential register(Credential entity) {
        logger.info("Registering credential with explicit relationship handling");
        
        // User is already validated and set by the controller, no need to re-fetch
        logger.info("User relationship already established: {}", entity.getUser().getUsername());
                
        // Ensure the credential type relationship is properly set by ZRN
        if (entity.getCredentialType() != null && entity.getCredentialType().getZrn() != null) {
            Optional<CredentialType> credentialType = credentialTypeRepo.findByZrn(
                entity.getCredentialType().getZrn()
            );
            if (credentialType.isPresent()) {
                entity.setCredentialType(credentialType.get());
                logger.info("Credential type relationship established: {}", credentialType.get().getZrn());
            } else {
                logger.error("Credential type not found: {}", entity.getCredentialType().getZrn());
                throw new RuntimeException("Tipo de credencial no encontrado: " + entity.getCredentialType().getZrn());
            }
        }
        
        return super.register(entity);
    }

    @Override
    public Credential modify(Credential entity) {
        logger.info("Modifying credential with explicit relationship handling");
        
        // User is already validated in the controller, no need to re-fetch
        logger.info("User relationship already established: {}", entity.getUser().getUsername());
        
        // Note: Namespace cannot be changed once assigned
        // The namespace relationship should remain as it was originally set
        if (entity.getNamespace() != null && entity.getNamespace().getId() != null) {
            Optional<Namespace> namespace = namespaceRepo.findById(entity.getNamespace().getId());
            if (namespace.isPresent()) {
                entity.setNamespace(namespace.get());
                logger.info("Namespace relationship maintained for credential update");
            }
        }
        
        // Handle credential type relationship if ZRN is provided for update
        if (entity.getCredentialType() != null && entity.getCredentialType().getZrn() != null) {
            Optional<CredentialType> credentialType = credentialTypeRepo.findByZrn(
                entity.getCredentialType().getZrn()
            );
            if (credentialType.isPresent()) {
                entity.setCredentialType(credentialType.get());
                logger.info("Credential type relationship updated: {}", credentialType.get().getZrn());
            } else {
                logger.error("Credential type not found: {}", entity.getCredentialType().getZrn());
                throw new RuntimeException("Tipo de credencial no encontrado: " + entity.getCredentialType().getZrn());
            }
        }
        
        return super.modify(entity);
    }

    @Override
    public List<Credential> getCredentialsByUserId(Long userId) {
        logger.info("Getting credentials for user ID: {}", userId);
        return credentialRepo.findByUserId(userId);
    }

    @Override
    public Optional<Credential> getCredentialByZrnAndUserId(String zrn, Long userId) {
        logger.info("Getting credential by ZRN: {} for user ID: {}", zrn, userId);
        return credentialRepo.findByZrnAndUserId(zrn, userId);
    }

    @Override
    public List<Credential> getCredentialsByNamespaceAndUserId(String namespaceZrn, Long userId) {
        logger.info("Getting credentials by namespace ZRN: {} for user ID: {}", namespaceZrn, userId);
        return credentialRepo.findByNamespaceZrnAndUserId(namespaceZrn, userId);
    }

    @Override
    public boolean existsCredentialByZrnAndUserId(String zrn, Long userId) {
        logger.info("Checking if credential exists by ZRN: {} for user ID: {}", zrn, userId);
        return credentialRepo.existsByZrnAndUserId(zrn, userId);
    }

    @Override
    protected IGenericRepo<Credential, Long> getRepo() {
        logger.info("Returning credential repository");
        return credentialRepo;
    }
}