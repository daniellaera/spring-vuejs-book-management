package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.properties.Oauth2Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GithubFeatureService {

    private final Oauth2Properties oauth2Properties;

    @Autowired
    public GithubFeatureService(Oauth2Properties oauth2Properties) {
        this.oauth2Properties = oauth2Properties;
    }

    public boolean isGithubFeatureEnabled() {
        return oauth2Properties.isEnabled();
    }
}
