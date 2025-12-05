package com.zekret.service.impl;

import java.util.List;

import org.jboss.logging.Logger;

import com.zekret.dto.NamespaceRequestDTO;
import com.zekret.dto.NamespaceResponseDTO;
import com.zekret.exception.ResourceNotFoundException;
import com.zekret.mapper.NamespaceMapper;
import com.zekret.model.Namespace;
import com.zekret.model.User;
import com.zekret.repository.NamespaceRepository;
import com.zekret.repository.UserRepository;
import com.zekret.service.INamespaceService;
import com.zekret.util.ZrnGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class NamespaceServiceImpl implements INamespaceService {
    private static final Logger LOG = Logger.getLogger(NamespaceServiceImpl.class);

    private final NamespaceRepository namespaceRepository;
    private final UserRepository userRepository;

    public NamespaceServiceImpl(NamespaceRepository namespaceRepository, UserRepository userRepository) {
        this.namespaceRepository = namespaceRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<NamespaceResponseDTO> getNamespacesByUserEmail(String userEmail) {
        LOG.infof("Fetching namespaces for user email: %s", userEmail);
        
        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                        .orElseThrow(() -> {
                            LOG.warnf("User with email %s not found.", userEmail);
                            return new ResourceNotFoundException("User", userEmail);
                        });
        List<NamespaceResponseDTO> namespaces = namespaceRepository.findByUserId(user.getId())
                                                .stream()
                                                .map(ns -> NamespaceMapper.toDTO(ns))
                                                .toList();
        return namespaces;
    }

    @Override
    public NamespaceResponseDTO getNamespaceByZrnAndUserEmail(String zrn, String userEmail) {
        LOG.infof("Fetching namespace with ZRN: %s for user email: %s", zrn, userEmail);
        
        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                        .orElseThrow(() -> {
                            LOG.warnf("User with email %s not found.", userEmail);
                            return new ResourceNotFoundException("User", userEmail);
                        });

        Namespace namespace = namespaceRepository.findByZrnAndUserId(zrn, user.getId())
                                    .orElseThrow(() -> {
                                        LOG.warnf("Namespace with ZRN %s not found for user %s.", zrn, userEmail);
                                        return new ResourceNotFoundException("Namespace", zrn);
                                    });
        return NamespaceMapper.toDTO(namespace);
    
    }

    @Override
    @Transactional
    public NamespaceResponseDTO registerNamespace(String userEmail, NamespaceRequestDTO namespace) {
        LOG.infof("Registering namespace for user email: %s", userEmail);

        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                        .orElseThrow(() -> {
                            LOG.warnf("User with email %s not found.", userEmail);
                            return new ResourceNotFoundException("User", userEmail);
                        });
        
        Namespace entity = NamespaceMapper.toEntity(namespace);
        entity.setZrn(ZrnGenerator.generateNamespaceZrn());
        entity.setUser(user);

        namespaceRepository.persist(entity);

        return NamespaceMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public NamespaceResponseDTO updateNamespace(String userEmail, String zrn, NamespaceRequestDTO namespace) {
        LOG.infof("Updating namespace for user email: %s", userEmail);
        
        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                        .orElseThrow(() -> {
                            LOG.warnf("User with email %s not found.", userEmail);
                            return new ResourceNotFoundException("User", userEmail);
                        });
        
        Namespace existingNamespace = namespaceRepository.findByZrnAndUserId(zrn, user.getId())
                                        .orElseThrow(() -> {
                                            LOG.warnf("Namespace with ZRN %s not found for user %s.", zrn, userEmail);
                                            return new ResourceNotFoundException("Namespace", zrn);
                                        });

        existingNamespace.setName(namespace.name());
        existingNamespace.setDescription(namespace.description());

        namespaceRepository.persist(existingNamespace);

        return NamespaceMapper.toDTO(existingNamespace);
    }

    @Override
    @Transactional
    public void deleteNamespaceByZrnAndUserEmail(String zrn, String userEmail) {
        LOG.infof("Deleting namespace with ZRN: %s for user email: %s", zrn, userEmail);
        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                        .orElseThrow(() -> {
                            LOG.warnf("User with email %s not found.", userEmail);
                            return new ResourceNotFoundException("User", userEmail);
                        });
        
        Namespace namespace = namespaceRepository.findByZrnAndUserId(zrn, user.getId())
                                    .orElseThrow(() -> {
                                        LOG.warnf("Namespace with ZRN %s not found for user %s.", zrn, userEmail);
                                        return new ResourceNotFoundException("Namespace", zrn);
                                    });
        namespaceRepository.delete(namespace);
    }
}