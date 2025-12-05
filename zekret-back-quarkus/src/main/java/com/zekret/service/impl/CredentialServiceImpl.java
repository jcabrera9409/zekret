package com.zekret.service.impl;

import java.util.List;

import org.jboss.logging.Logger;

import com.zekret.dto.CredentialRequestDTO;
import com.zekret.dto.CredentialResponseDTO;
import com.zekret.exception.ResourceNotFoundException;
import com.zekret.mapper.CredentialMapper;
import com.zekret.model.Credential;
import com.zekret.model.CredentialType;
import com.zekret.model.Namespace;
import com.zekret.model.User;
import com.zekret.repository.CredentialRepository;
import com.zekret.repository.CredentialTypeRepository;
import com.zekret.repository.NamespaceRepository;
import com.zekret.repository.UserRepository;
import com.zekret.service.ICredentialService;
import com.zekret.util.ZrnGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CredentialServiceImpl implements ICredentialService {
    private static final Logger LOG = Logger.getLogger(CredentialServiceImpl.class.getName());

    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository; 
    private final CredentialTypeRepository credentialTypeRepository;
    private final NamespaceRepository namespaceRepository;

    public CredentialServiceImpl(CredentialRepository credentialRepository, UserRepository userRepository, CredentialTypeRepository credentialTypeRepository, NamespaceRepository namespaceRepository) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.credentialTypeRepository = credentialTypeRepository;
        this.namespaceRepository = namespaceRepository;
    }

    @Override
    public List<CredentialResponseDTO> getCredentialsByNamespaceAndUserId(String namespaceZrn, String userEmail) {
        LOG.info("Getting credentials for namespaceZrn: " + namespaceZrn + " and userEmail: " + userEmail);

        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        
        List<CredentialResponseDTO> credentials = credentialRepository.
                                                findByNamespaceZrnAndUserId(namespaceZrn, user.getId())
                                                .stream()
                                                .map(credential -> CredentialMapper.toDTO(credential))
                                                .toList();
        return credentials;
    }

    @Override
    public CredentialResponseDTO getCredentialByZrnAndUserEmail(String zrn, String userEmail) {
        LOG.info("Getting credential for zrn: " + zrn + " and userEmail: " + userEmail);

        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        
        Credential credential = credentialRepository
                                .findByZrnAndUserId(zrn,  user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Credential", zrn));
        
        return CredentialMapper.toDTO(credential);
    }

    @Override
    @Transactional
    public CredentialResponseDTO registerCredential(String userEmail, CredentialRequestDTO credentialRequestDTO) {
        LOG.info("Registering credential for userEmail: " + userEmail);
        
        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        CredentialType credentialType = credentialTypeRepository.findByZrn(credentialRequestDTO.credentialTypeZrn())
                .orElseThrow(() -> new ResourceNotFoundException("CredentialType", credentialRequestDTO.credentialTypeZrn()));

        Namespace namespace = namespaceRepository.findByZrnAndUserId(credentialRequestDTO.namespaceZrn(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Namespace", credentialRequestDTO.namespaceZrn()));
        
        Credential credential = CredentialMapper.toEntity(credentialRequestDTO);
        credential.setZrn(ZrnGenerator.generateCredentialZrn());
        credential.setCredentialType(credentialType);
        credential.setNamespace(namespace);
        credential.setUser(user);

        credentialRepository.persist(credential);

        return CredentialMapper.toDTO(credential);
    }

    @Override
    @Transactional
    public CredentialResponseDTO updateCredential(String userEmail, String zrn, CredentialRequestDTO credentialRequestDTO) {
        LOG.info("Updating credential for zrn: " + zrn + " and userEmail: " + userEmail);

        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        
        Credential credential = credentialRepository
                                .findByZrnAndUserId(zrn,  user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Credential", zrn));

        CredentialType credentialType = credentialTypeRepository.findByZrn(credentialRequestDTO.credentialTypeZrn())
                .orElseThrow(() -> new ResourceNotFoundException("CredentialType", credentialRequestDTO.credentialTypeZrn()));


        Namespace namespace = namespaceRepository.findByZrnAndUserId(credentialRequestDTO.namespaceZrn(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Namespace", credentialRequestDTO.namespaceZrn()));

        // Actualizar la entidad existente en lugar de crear una nueva
        credential.setTitle(credentialRequestDTO.title());
        credential.setUsername(credentialRequestDTO.username());
        credential.setPassword(credentialRequestDTO.password());
        credential.setSshPublicKey(credentialRequestDTO.sshPublicKey());
        credential.setSshPrivateKey(credentialRequestDTO.sshPrivateKey());
        credential.setSecretText(credentialRequestDTO.secretText());
        credential.setFileName(credentialRequestDTO.fileName());
        credential.setFileContent(credentialRequestDTO.fileContent());
        credential.setNotes(credentialRequestDTO.notes());
        credential.setCredentialType(credentialType);
        credential.setNamespace(namespace);
        // No es necesario persist() ni merge(), Hibernate detecta los cambios automáticamente

        return CredentialMapper.toDTO(credential);
    }

    @Override
    @Transactional
    public void deleteCredentialByZrnAndUserEmail(String zrn, String userEmail) {
        LOG.info("Deleting credential for zrn: " + zrn + " and userEmail: " + userEmail);

        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        
        Credential credential = credentialRepository
                                .findByZrnAndUserId(zrn,  user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Credential", zrn));

        credentialRepository.delete(credential);
    }
}