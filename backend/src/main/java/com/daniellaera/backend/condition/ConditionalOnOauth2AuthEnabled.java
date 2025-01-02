package com.daniellaera.backend.condition;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(name = "social.feature.oauth2.enabled")
public @interface ConditionalOnOauth2AuthEnabled {
}