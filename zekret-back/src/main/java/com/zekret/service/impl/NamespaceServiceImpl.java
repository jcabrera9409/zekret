package com.zekret.service.impl;

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
    protected IGenericRepo<Namespace, Long> getRepo() {
        logger.info("Returning namespace repository");
        return namespaceRepo;
    }
}