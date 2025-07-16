package com.zekret.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zekret.model.CredentialType;
import com.zekret.repo.ICredentialTypeRepo;
import com.zekret.repo.IGenericRepo;
import com.zekret.service.ICredentialTypeService;

@Service
public class CredentialTypeServiceImpl extends CRUDImpl<CredentialType, Long> implements ICredentialTypeService {

	private static final Logger logger = LoggerFactory.getLogger(CredentialTypeServiceImpl.class);

    private final ICredentialTypeRepo credentialTypeRepo;

    public CredentialTypeServiceImpl(ICredentialTypeRepo credentialTypeRepo) {
        this.credentialTypeRepo = credentialTypeRepo;
    }

    @Override
    protected IGenericRepo<CredentialType, Long> getRepo() {
    	logger.info("Returning credential type repository");
        return credentialTypeRepo;
    }
}