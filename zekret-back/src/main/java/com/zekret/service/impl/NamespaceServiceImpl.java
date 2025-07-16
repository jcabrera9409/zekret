package com.zekret.service.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zekret.model.Namespace;
import com.zekret.repo.INamespaceRepo;
import com.zekret.repo.IGenericRepo;
import com.zekret.service.INamespaceService;

@Service
public class NamespaceServiceImpl extends CRUDImpl<Namespace, Long> implements INamespaceService {

    private static final Logger logger = LoggerFactory.getLogger(NamespaceServiceImpl.class);

    private final INamespaceRepo namespaceRepo;

    public NamespaceServiceImpl(INamespaceRepo namespaceRepo) {
        this.namespaceRepo = namespaceRepo;
    }

    @Override
    public Namespace register(Namespace entity) {
        logger.info("Registering namespace with explicit user relationship handling");
        
        // User is already validated and set by the controller, no need to re-fetch
        logger.info("User relationship already established: {}", entity.getUser().getUsername());
        
        return super.register(entity);
    }

    @Override
    public Namespace modify(Namespace entity) {
        logger.info("Modifying namespace - user relationship maintained from controller");
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