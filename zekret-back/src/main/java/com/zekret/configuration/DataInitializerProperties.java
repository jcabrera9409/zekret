package com.zekret.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.zekret.model.CredentialType;

@Configuration
@ConfigurationProperties(prefix = "data-initializer")
public class DataInitializerProperties {
    private CredentialType[] credentialType;

    public CredentialType[] getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(CredentialType[] credentialType) {
        this.credentialType = credentialType;
    }
}
