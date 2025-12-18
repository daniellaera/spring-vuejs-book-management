package com.daniellaera.backend.controller;

import com.daniellaera.backend.properties.VersionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v3")
@Slf4j
public class VersionController {

    private final VersionProperties versionProperties;

    public VersionController(VersionProperties versionProperties) {
        this.versionProperties = versionProperties;
    }

    /**
     * Returns the current application version and build time.
     *
     * @return Map with version and buildTime
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        String version = versionProperties.getVersion();
        String buildTime = versionProperties.getBuildTime();

        log.info("Version endpoint called - returning version: {}, buildTime: {}", version, buildTime);

        return ResponseEntity.ok(Map.of(
                "version", version,
                "buildTime", buildTime
        ));
    }
}
