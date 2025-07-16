package com.zekret.service.impl;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zekret.model.Credential;
import com.zekret.repo.ICredentialRepo;
import com.zekret.repo.IGenericRepo;
import com.zekret.service.ICredentialService;

@Service
public class CredentialServiceImpl extends CRUDImpl<Credential, Long> implements ICredentialService {

    private static final Logger logger = LoggerFactory.getLogger(CredentialServiceImpl.class);

    private final ICredentialRepo credentialRepo;

    public CredentialServiceImpl(ICredentialRepo credentialRepo) {
        this.credentialRepo = credentialRepo;
    }

    @Override
    protected IGenericRepo<Credential, Long> getRepo() {
        logger.info("Returning credential repository");
        return credentialRepo;
    }
}