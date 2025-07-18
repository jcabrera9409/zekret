package com.zekret.configuration;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.zekret.model.CredentialType;
import com.zekret.repo.ICredentialTypeRepo;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private DataInitializerProperties properties;

    @Autowired
    private ICredentialTypeRepo credentialTypeRepo;

    @Override
    public void run(String... args) throws Exception {
        initializeCredentialTypes();
    }

    private void initializeCredentialTypes() {
        // Validar que las propiedades no sean null
        if (properties == null || properties.getCredentialType() == null) {
            System.err.println("Warning: data-initializer.credentialType configuration not found or null");
            return;
        }
        
        for (CredentialType type : properties.getCredentialType()) {
            Optional<CredentialType> existingType = credentialTypeRepo.findByZrn(type.getZrn());
            if(existingType.isPresent()) {
                CredentialType existing = existingType.get();
                existing.setName(type.getName());
                credentialTypeRepo.save(existing);
            } else {
                CredentialType newType = new CredentialType();
                newType.setZrn(type.getZrn());
                newType.setName(type.getName());
                credentialTypeRepo.save(newType);
            }
        }
    }
}
