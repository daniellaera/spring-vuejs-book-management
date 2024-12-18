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
import com.daniellaera.backend.utils.NameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

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
    public User updateUser(String userEmail, SignUpRequest updatedUserData) {
        log.info("Loading user by username: {}", userEmail);

        User existingUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", userEmail);
                    return new UsernameNotFoundException("User not found with email: " + userEmail);
                });

        existingUser.setFirstName(updatedUserData.getFirstName());
        existingUser.setLastName(updatedUserData.getLastName());

        return userRepository.save(existingUser);
    }

    @Override
    public JwtAuthenticationResponse signupOrSigninWithGitHub(String email, String githubId) {
        Optional<User> existingUserByEmail = userRepository.findByEmail(email);

        if (existingUserByEmail.isPresent()) {
            log.info("User with email '{}' already exists. Logging in.", email);
            return generateJwtResponse(existingUserByEmail.get());
        }

        // Check if user exists by GitHub ID
        Optional<User> existingUserByGithubId = userRepository.findByGithubId(githubId);

        if (existingUserByGithubId.isPresent()) {
            log.info("User with githubId '{}' found. Updating email to '{}'.", githubId, email);

            User existingUser = existingUserByGithubId.get();
            existingUser.setEmail(email); // Update email
            userRepository.save(existingUser);

            return generateJwtResponse(existingUser);
        }

        // Create a new user
        log.info("No existing user found. Creating a new user with GitHub ID '{}'.", githubId);

        User newUser = User.builder()
                .email(email)
                .githubId(githubId)
                .firstName(NameGenerator.generateRandomName())
                .lastName(NameGenerator.generateUniqueSuffix())
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Secure temporary password
                .role(Role.USER)
                .build();

        userRepository.save(newUser);

        return generateJwtResponse(newUser);
    }

    private JwtAuthenticationResponse generateJwtResponse(User user) {
        String token = jwtService.generateToken(user.getUsername(), user.getRole().toString());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return JwtAuthenticationResponse
                .builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .username(user.getFirstName() + " " + user.getLastName())
                .build();
    }
}
