package com.zekret.repository;

import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;

import com.zekret.model.Credential;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CredentialRepository implements PanacheRepository<Credential> {
    private static final Logger LOG = Logger.getLogger(CredentialRepository.class.getName());

    /**
     * Find a credential by ZRN and user ID
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     * @return Credential if found, null otherwise
     */
    public Optional<Credential> findByZrnAndUserId(String zrn, Long userId) {
        LOG.info("Finding credential for zrn: " + zrn + " and userId: " + userId);
        return find("zrn = :zrn and user.id = :userId", 
                    Parameters.with("zrn", zrn).and("userId", userId)).firstResultOptional();
    }

    /**
     * Final all credentials for a specific user and namespace ZRN
     * @param namespaceZrn The namespace ZRN
     * @param userId The user ID to filter by
     * @return List of credentials in the namespace belonging to the user
     */
    public List<Credential> findByNamespaceZrnAndUserId(String namespaceZrn, Long userId) {
        LOG.info("Finding credentials for namespaceZrn: " + namespaceZrn + " and userId: " + userId);
        return list("namespace.zrn = :namespaceZrn and user.id = :userId", 
                    Parameters.with("namespaceZrn", namespaceZrn).and("userId", userId));
    }
}