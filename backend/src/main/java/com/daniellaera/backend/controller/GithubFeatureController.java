package com.daniellaera.backend.controller;

import com.daniellaera.backend.service.impl.GithubFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v3/features")
public class GithubFeatureController {

    private final GithubFeatureService githubFeatureService;

    @Autowired
    public GithubFeatureController(GithubFeatureService githubFeatureService) {
        this.githubFeatureService = githubFeatureService;
    }

    @GetMapping
    public Map<String, Boolean> getFeatures() {
        return Map.of("oauth2Enabled", githubFeatureService.isGithubFeatureEnabled());
    }

}
