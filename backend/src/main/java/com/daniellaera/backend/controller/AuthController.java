package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.request.RefreshTokenRequest;
import com.daniellaera.backend.dao.request.SignUpRequest;
import com.daniellaera.backend.dao.request.SigningRequest;
import com.daniellaera.backend.dao.response.JwtAuthenticationResponse;
import com.daniellaera.backend.dao.response.RefreshTokenResponse;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.service.AuthenticationService;
import com.daniellaera.backend.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v3/auth")
public class AuthController {

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;
    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;
    @Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
    private String redirectUri;

    // check this out
    // https://medium.com/spring-boot/spring-boot-3-spring-security-6-jwt-authentication-authorization-98702d6313a5

    private final RestTemplate restTemplate = new RestTemplate();

    private AuthenticationService authenticationService;
    private RefreshTokenService refreshTokenService;

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService, RefreshTokenService refreshTokenService) {
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
    }

    @GetMapping("/github/login")
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

    @PostMapping("/github/callback")
    public ResponseEntity<?> handleGitHubCallback(@RequestBody Map<String, String> requestBody) {
        String code = requestBody.get("code");

        if (code == null || code.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authorization code is required");
        }

        System.out.println("Received GitHub callback with code: " + code);

        // Exchange the code for an access token
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
                String username = (userResponse.getBody().get("name") != null) ? userResponse.getBody().get("name").toString() : "no-username-found";

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

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal != null) {
            // Get the Authentication object from SecurityContextHolder
            Authentication authentication = (Authentication) principal;
            // Extract the UserDetails (your custom User object) from the Authentication object
            User user = (User) authentication.getPrincipal();

            return ResponseEntity.ok(Map.of(
                    "username", user.getUsername(),
                    "fullName", user.getFullName()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {
        try {
            JwtAuthenticationResponse response = authenticationService.signup(request);
            return ResponseEntity.accepted().body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User with email '" + request.getEmail() + "' already exists");
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> signing(@RequestBody SigningRequest request) {
        //RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(authenticationService.signing(request));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.generateNewToken(request));
    }

}
