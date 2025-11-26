package com.daniellaera.backend.it;

import com.daniellaera.backend.service.impl.GithubFeatureService;
import com.daniellaera.backend.utils.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "social.feature.oauth2.enabled=false"
})
public class GithubFeatureServiceITTest {

    @Autowired
    private GithubFeatureService githubFeatureService;

    @Test
    void testGithubFeatureEnabled() {
        assertFalse(githubFeatureService.isGithubFeatureEnabled(), "Feature should be disabled");
    }
}
