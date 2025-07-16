package com.zekret.repo;

import java.util.Optional;

import com.zekret.model.CredentialType;

public interface ICredentialTypeRepo extends IGenericRepo<CredentialType, Long> {
    
    /**
     * Find credential type by ZRN using Spring Data JPA naming convention
     */
    Optional<CredentialType> findByZrn(String zrn);
}
