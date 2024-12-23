package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.request.SignUpRequest;
import com.daniellaera.backend.dao.request.SigningRequest;
import com.daniellaera.backend.dao.response.JwtAuthenticationResponse;
import com.daniellaera.backend.model.User;
import org.springframework.stereotype.Service;

@Service
public interface AuthenticationService {
    JwtAuthenticationResponse signup(SignUpRequest request);

    JwtAuthenticationResponse signing(SigningRequest request);

    JwtAuthenticationResponse signupOrSigninWithGitHub(String email, String username);

    User updateUser(String userEmail, SignUpRequest updatedUserData);
}
