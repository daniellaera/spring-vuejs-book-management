package com.daniellaera.backend.controller;

import com.daniellaera.backend.properties.VersionProperties;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class VersionController {

    private final VersionProperties versionProperties;

    @Autowired
    public VersionController(VersionProperties versionProperties) {
        this.versionProperties = versionProperties;
        // Log version info at startup
        log.info("Application initialized - Version: {}, Build Time: {}",
                versionProperties.getVersion(),
                versionProperties.getBuildTime());
    }

    @GetMapping("/version")
    public Map<String, String> getVersion() {
        log.debug("Version endpoint called - returning version: {}",
                versionProperties.getVersion());
        return Map.of(
                "version", versionProperties.getVersion(),
                "buildTime", versionProperties.getBuildTime()
        );
    }
}
