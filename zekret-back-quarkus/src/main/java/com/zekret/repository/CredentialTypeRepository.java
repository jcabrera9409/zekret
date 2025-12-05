package com.zekret.repository;

import java.util.Optional;

import org.jboss.logging.Logger;

import com.zekret.model.CredentialType;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CredentialTypeRepository implements PanacheRepository<CredentialType> {
    private static final Logger LOG = Logger.getLogger(CredentialTypeRepository.class);
    
    /**
     * Find a CredentialType by its ZRN.
     * @param zrn the ZRN of the CredentialType
     * @return the CredentialType if found, otherwise null
     */
    public Optional<CredentialType> findByZrn(String zrn) {
        LOG.debugf("Finding CredentialType by ZRN: %s", zrn);
        return find("zrn", zrn).firstResultOptional();
    }
}