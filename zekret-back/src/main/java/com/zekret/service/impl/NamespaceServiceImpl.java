package com.zekret.service.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zekret.model.Namespace;
import com.zekret.model.User;
import com.zekret.repo.INamespaceRepo;
import com.zekret.repo.IUserRepo;
import com.zekret.repo.IGenericRepo;
import com.zekret.service.INamespaceService;

@Service
public class NamespaceServiceImpl extends CRUDImpl<Namespace, Long> implements INamespaceService {

    private static final Logger logger = LoggerFactory.getLogger(NamespaceServiceImpl.class);

    private final INamespaceRepo namespaceRepo;
    
    @Autowired
    private IUserRepo userRepo;

    public NamespaceServiceImpl(INamespaceRepo namespaceRepo) {
        this.namespaceRepo = namespaceRepo;
    }

    @Override
    public Namespace register(Namespace entity) {
        logger.info("Registering namespace with explicit user relationship handling");
        
        // Ensure the user relationship is properly set by ID
        if (entity.getUser() != null && entity.getUser().getId() != null) {
            Optional<User> user = userRepo.findById(entity.getUser().getId());
            if (user.isPresent()) {
                entity.setUser(user.get());
                logger.info("User relationship established for namespace");
            } else {
                logger.error("User not found with ID: {}", entity.getUser().getId());
                throw new RuntimeException("User not found with ID: " + entity.getUser().getId());
            }
        }
        
        return super.register(entity);
    }

    @Override
    public Namespace modify(Namespace entity) {
        logger.info("Modifying namespace with explicit user relationship handling");
        
        // Ensure the user relationship is properly maintained
        if (entity.getUser() != null && entity.getUser().getId() != null) {
            Optional<User> user = userRepo.findById(entity.getUser().getId());
            if (user.isPresent()) {
                entity.setUser(user.get());
                logger.info("User relationship maintained for namespace update");
            } else {
                logger.error("User not found with ID: {}", entity.getUser().getId());
                throw new RuntimeException("User not found with ID: " + entity.getUser().getId());
            }
        }
        
        return super.modify(entity);
    }

    @Override
    public List<Namespace> getNamespacesByUserId(Long userId) {
        logger.info("Getting namespaces for user ID: {}", userId);
        return namespaceRepo.findByUserId(userId);
    }

    @Override
    public Optional<Namespace> getNamespaceByZrnAndUserId(String zrn, Long userId) {
        logger.info("Getting namespace by ZRN: {} for user ID: {}", zrn, userId);
        return namespaceRepo.findByZrnAndUserId(zrn, userId);
    }

    @Override
    public boolean existsNamespaceByZrnAndUserId(String zrn, Long userId) {
        logger.info("Checking if namespace exists by ZRN: {} for user ID: {}", zrn, userId);
        return namespaceRepo.existsByZrnAndUserId(zrn, userId);
    }

    @Override
    protected IGenericRepo<Namespace, Long> getRepo() {
        logger.info("Returning namespace repository");
        return namespaceRepo;
    }
}