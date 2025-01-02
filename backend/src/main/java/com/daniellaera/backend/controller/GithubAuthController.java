package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.response.JwtAuthenticationResponse;
import com.daniellaera.backend.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v3/github")
public class GithubAuthController {

    private final RestTemplate restTemplate = new RestTemplate();

    private AuthenticationService authenticationService;

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;
    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;
    @Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
    private String redirectUri;

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> redirectToGithubOAuth() {
        // Construct the GitHub OAuth URL
        String authorizationUrl = "https://github.com/login/oauth/authorize"
                + "?client_id=" + githubClientId
                + "&redirect_uri=" + redirectUri
                + "&scope=user:email"; // Optional: Add any required scope

        // Send the URL back to the frontend so it can redirect the user
        Map<String, String> response = new HashMap<>();
        response.put("authUrl", authorizationUrl);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/callback")
    public ResponseEntity<?> handleGitHubCallback(@RequestBody Map<String, String> requestBody) {
        String code = requestBody.get("code");

        if (code == null || code.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authorization code is required");
        }

        System.out.println("Received GitHub callback with code: " + code);

        // Exchange the code for an access token
        // https://stackoverflow.com/questions/64960385/how-can-i-setup-login-with-spring-security-and-vue-js

        String tokenEndpoint = "https://github.com/login/oauth/access_token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, String> body = new HashMap<>();
        body.put("client_id", githubClientId);
        body.put("client_secret", githubClientSecret);
        body.put("code", code);
        body.put("redirect_uri", redirectUri);  // Make sure this matches the one used in the GitHub authorization request

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenEndpoint, requestEntity, Map.class);

        if (response.getBody() != null && response.getBody().containsKey("access_token")) {
            String accessToken = response.getBody().get("access_token").toString();

            // Fetch user details from GitHub API
            ResponseEntity<Map> userResponse = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders(accessToken)),
                    Map.class
            );

            if (userResponse.getBody() != null) {
                String email = (userResponse.getBody().get("email") != null) ? userResponse.getBody().get("email").toString() : "no-email-found";
                String githubId = userResponse.getBody().get("id").toString(); // Unique GitHub user ID
                String username = userResponse.getBody().get("name") != null ? userResponse.getBody().get("name").toString() : githubId;

                try {
                    JwtAuthenticationResponse jwtResponse = authenticationService.signupOrSigninWithGitHub(email, username);
                    return ResponseEntity.ok(Collections.singletonMap("token", jwtResponse.getToken()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("GitHub user already exists with email: " + email);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("GitHub authentication failed");
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
