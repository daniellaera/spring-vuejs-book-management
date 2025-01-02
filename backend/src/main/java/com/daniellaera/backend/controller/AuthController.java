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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v3/auth")
public class AuthController {

    // check this out
    // https://medium.com/spring-boot/spring-boot-3-spring-security-6-jwt-authentication-authorization-98702d6313a5

    private AuthenticationService authenticationService;
    private RefreshTokenService refreshTokenService;

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService, RefreshTokenService refreshTokenService) {
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal != null) {
            // Get the Authentication object from SecurityContextHolder
            Authentication authentication = (Authentication) principal;
            // Extract the UserDetails (your custom User object) from the Authentication object
            User user = (User) authentication.getPrincipal();

            // Create a mutable map to add fields dynamically
            Map<String, Object> response = new HashMap<>();
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("userId", user.getId());

            // Conditionally add githubId if it is not null
            if (user.getGithubId() != null) {
                response.put("githubId", user.getGithubId());
            }

            return ResponseEntity.ok(response);
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

    @PatchMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> updateUser(
            @AuthenticationPrincipal User currentUser, // Get the logged-in user
            @RequestBody SignUpRequest updatedUserData) {
        if (currentUser.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String userEmail = currentUser.getEmail();
        try {
            User updatedUser = authenticationService.updateUser(userEmail, updatedUserData);
            return ResponseEntity.ok(Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "firstName", updatedUser.getFirstName(),
                    "lastName", updatedUser.getLastName()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
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
