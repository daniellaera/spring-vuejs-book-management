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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
