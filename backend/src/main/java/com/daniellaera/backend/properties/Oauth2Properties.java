package com.daniellaera.backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "social.feature.oauth2")
public class Oauth2Properties {

    /**
     * Indicates whether OAuth2 is enabled.
     */
    private boolean enabled;

}

