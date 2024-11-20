package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.dao.request.SignUpRequest;
import com.daniellaera.backend.dao.request.SigningRequest;
import com.daniellaera.backend.dao.response.JwtAuthenticationResponse;
import com.daniellaera.backend.model.RefreshToken;
import com.daniellaera.backend.model.Role;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.AuthenticationService;
import com.daniellaera.backend.service.JwtService;
import com.daniellaera.backend.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    public JwtAuthenticationResponse signup(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("User with email '{}' already exists", request.getEmail());
            throw new IllegalArgumentException("User with email '" + request.getEmail() + "' already exists");
        }

        // Create a new user
        User user = User
                .builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        // Save the new user
        userRepository.save(user);

        // Generate JWT token for the user
        String jwt = jwtService.generateToken(user.getUsername(), user.getRole().toString());

        // Return the JWT token in the response
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }

    @Override
    public JwtAuthenticationResponse signing(SigningRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            User user = (User) authentication.getPrincipal();
            String jwt = jwtService.generateToken(user.getUsername(), user.getRole().toString());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
            return JwtAuthenticationResponse
                    .builder()
                    .token(jwt)
                    .refreshToken(refreshToken.getToken())
                    .username(user.getFirstName() + " " + user.getLastName())
                    .build();
        } catch (BadCredentialsException e) {
            log.error("Invalid email or password for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @Override
    public JwtAuthenticationResponse signupOrSigninWithGitHub(String email, String username) {
        // Check if the user already exists in the database using the GitHub email
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            log.info("User with email '{}' already exists", email);
            log.info("Login the user and creating a JWT token");
            // User exists, perform sign-in
            User user = existingUser.get();
            // Generate JWT token for the user
            String token = jwtService.generateToken(user.getUsername(), "USER");
            // Generate a refresh token for the user
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
            // Return the JWT and refresh token in the response
            return JwtAuthenticationResponse
                    .builder()
                    .token(token)
                    .refreshToken(refreshToken.getToken())
                    .username(user.getFirstName() + " " + user.getLastName())
                    .build();
        } else {
            log.info("User with email '{}' does not exists", email);
            log.info("Signup the user and creating a JWT token");
            // User does not exist, perform sign-up manually using GitHub details
            String[] nameParts = username.split(" ", 2); // Split into first and last name
            String firstName = nameParts[0];  // First part as first name
            String lastName = nameParts[1];
            User newUser = User.builder()
                    .email(email)         // Use the GitHub email
                    .firstName(firstName)   // Default first name, can be replaced with actual GitHub user data if needed
                    .lastName(lastName)      // Default last name, can be replaced with actual GitHub user data if needed
                    .password(passwordEncoder.encode("temporarypassword"))  // Set a temporary password, can later be updated
                    .role(Role.USER)       // Set the role as "USER"
                    .build();

            // Save the new user in the database
            userRepository.save(newUser);

            // Generate JWT token for the new user
            String token = jwtService.generateToken(newUser.getUsername(), newUser.getRole().toString());

            // Generate a refresh token for the new user
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(newUser.getId());

            // Return the JWT and refresh token in the response
            return JwtAuthenticationResponse
                    .builder()
                    .token(token)
                    .refreshToken(refreshToken.getToken())
                    .username(newUser.getFirstName() + " " + newUser.getLastName()) // Provide the user's full name
                    .build();
        }
    }
}
