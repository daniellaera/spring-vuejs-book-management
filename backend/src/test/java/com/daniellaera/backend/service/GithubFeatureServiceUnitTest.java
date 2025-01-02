package com.daniellaera.backend.service;

import com.daniellaera.backend.properties.Oauth2Properties;
import com.daniellaera.backend.service.impl.GithubFeatureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GithubFeatureServiceUnitTest {

    @Mock
    private Oauth2Properties oauth2Properties;

    @InjectMocks
    private GithubFeatureService githubFeatureService;

    @Test
    void testGithubFeatureDisabled() {
        when(oauth2Properties.isEnabled()).thenReturn(false);

        assertFalse(githubFeatureService.isGithubFeatureEnabled(), "Feature should be disabled");
    }

    @Test
    void testGithubFeatureEnabled() {
        when(oauth2Properties.isEnabled()).thenReturn(true);

        assertTrue(githubFeatureService.isGithubFeatureEnabled(), "Feature should be enabled");
    }
}
