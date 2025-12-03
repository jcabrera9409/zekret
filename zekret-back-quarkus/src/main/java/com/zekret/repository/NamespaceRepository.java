package com.zekret.repository;

import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;

import com.zekret.model.Namespace;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NamespaceRepository implements PanacheRepository<Namespace> {
    private static final Logger LOG = Logger.getLogger(NamespaceRepository.class);

    /**
     * Find all namespaces for a specific user
     * @param userId The user ID to filter by
     * @return List of namespaces belonging to the user
     */
    List<Namespace> findByUserId(Long userId) {
        LOG.debugf("Finding namespaces for userId: %d", userId);
        return list("user.id", userId);
    }

    /**
     * Find a namespace by ZRN and user ID
     * @param zrn The ZRN identifier
     * @param userId The user ID to filter by
     * @return Namespace if found, null otherwise
     */
    Optional<Namespace> findByZrnAndUserId(String zrn, Long userId) {
        LOG.debugf("Finding namespace for zrn: %s and userId: %d", zrn, userId);
        Optional<Namespace> namespace = find("zrn = :zrn and user.id = :userId", 
                    Parameters.with("zrn", zrn).and("userId", userId)).firstResultOptional();
        return namespace;
    }
}
