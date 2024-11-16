package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.request.SignUpRequest;
import com.daniellaera.backend.dao.request.SigningRequest;
import com.daniellaera.backend.dao.response.JwtAuthenticationResponse;
import com.daniellaera.backend.model.RefreshToken;
import com.daniellaera.backend.model.Role;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.impl.AuthenticationServiceImpl;
import com.daniellaera.backend.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * author daniellaera on 08.04.2024
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private JwtService jwtService; // Mock JwtService

    private AuthenticationService authenticationService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private RefreshTokenService refreshTokenService;
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        refreshTokenService = mock(RefreshTokenService.class);
        authenticationManager = mock(AuthenticationManager.class);
        authenticationService = new AuthenticationServiceImpl(userRepository, passwordEncoder, jwtService, authenticationManager, refreshTokenService);
    }

    @Test
    void signup_ValidRequest_ReturnsJwtAuthenticationResponse() {
        // Arrange
        SignUpRequest request = new SignUpRequest("John", "Doe", "john.doe@example.com", "123456");
        User user = TestUtils.createUserDto("John", "Doe", "john.doe@example.com", "encodedPassword", Role.USER);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        String expectedToken = jwtService.generateToken(user.getUsername(), user.getRole().toString());

        // Act
        JwtAuthenticationResponse response = authenticationService.signup(request);

        // Assert
        assertEquals(expectedToken, response.getToken());
        assertEquals("USER", user.getRole().toString()); // Verify the role here
    }

    @Test
    void signing_ValidCredentials_ReturnsJwtAuthenticationResponse() {
        // Arrange
        String email = "john.doe@example.com";
        String password = "password123";
        SigningRequest request = new SigningRequest(email, password);
        User user = TestUtils.createUserDto("John", "Doe", email, "encodedPassword", Role.USER);

        // Mock the behavior of authenticationManager.authenticate to return a valid Authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        // Mock the behavior of createRefreshToken method to return a predefined refresh token
        RefreshToken refreshToken = new RefreshToken(); // Create a new RefreshToken instance
        refreshToken.setToken("someToken");

        when(refreshTokenService.createRefreshToken(user.getId())).thenReturn(refreshToken);

        // Act
        JwtAuthenticationResponse response = authenticationService.signing(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getRefreshToken());
        assertEquals("USER", user.getRole().toString());
    }

    @Test
    void signing_InvalidCredentials_ThrowsBadCredentialsException() {
        // Arrange
        SigningRequest request = new SigningRequest("john.doe@example.com", "invalidPassword");
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act and Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> authenticationService.signing(request));
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void signup_UserAlreadyExists_ThrowsIllegalArgumentException() {
        // Arrange
        SignUpRequest request = new SignUpRequest("John", "Doe", "john.doe@example.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true); // Simulating user already exists

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(request));
        assertEquals("User with email 'john.doe@example.com' already exists", exception.getMessage());
    }

    @Test
    void signup_UserDoesNotExist_ReturnsJwtAuthenticationResponse() {
        // Arrange
        SignUpRequest request = new SignUpRequest("John", "Doe", "john.doe@example.com", "123456");
        User user = TestUtils.createUserDto("John", "Doe", "john.doe@example.com", "encodedPassword", Role.USER);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        String expectedToken = jwtService.generateToken(user.getUsername(), user.getRole().toString());

        // Act
        JwtAuthenticationResponse response = authenticationService.signup(request);

        // Assert
        assertEquals(expectedToken, response.getToken());
        assertEquals("USER", user.getRole().toString());
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        // Arrange
        String expectedUsername = "john.doe@example.com";
        String mockToken = "mockJwtToken"; // Mock a valid token (this will be the token returned by the mocked `generateToken` method)

        // Mock the behavior of generateToken to return the mock token

        // Mock the behavior of extractUsername to return the expected username from the mock token
        when(jwtService.extractUsername(mockToken)).thenReturn(expectedUsername);

        // Act
        String extractedUsername = jwtService.extractUsername(mockToken);

        // Assert
        assertEquals(expectedUsername, extractedUsername);
    }

    @Test
    void extractExpiration_ValidToken_ReturnsExpirationDate() {
        // Arrange
        String fixedToken = "mockJwtToken"; // Fixed mock token

        // Mock expiration time
        Date expectedExpirationDate = new Date(System.currentTimeMillis() + 3600000); // 1 hour later
        when(jwtService.extractExpiration(fixedToken)).thenReturn(expectedExpirationDate);

        // Act
        Date expirationDate = jwtService.extractExpiration(fixedToken);

        // Assert
        assertEquals(expectedExpirationDate, expirationDate); // Compare with the mock expiration date
    }


}
