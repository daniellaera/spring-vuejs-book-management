package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.request.RefreshTokenRequest;
import com.daniellaera.backend.dao.response.RefreshTokenResponse;
import com.daniellaera.backend.model.RefreshToken;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.RefreshTokenRepository;
import com.daniellaera.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.jwtRefreshExpirationMs}")
    private Long refreshExpiration;

    @Autowired
    public RefreshTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                               JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public RefreshToken createRefreshToken(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Log the current time
        Instant currentTime = Instant.now();
        log.info("Current time: {}", currentTime);

        // Get the time zone for Paris
        ZoneId parisZone = ZoneId.of("Europe/Paris");

        // Convert the current time to Paris time zone
        ZonedDateTime parisTime = Instant.now().atZone(parisZone);
        log.info("Current time in {}: {}", parisZone, parisTime);

        // Check if there's an existing refresh token for the user
        RefreshToken existingToken = refreshTokenRepository.findByUserId(userId).orElse(null);

        // If an existing token exists, update its expiration date
        if (existingToken != null) {
            existingToken.setExpiryDate(parisTime.toInstant().plusMillis(refreshExpiration));
            return refreshTokenRepository.save(existingToken);
        }

        // Otherwise, create a new refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes()))
                .expiryDate(parisTime.toInstant().plusMillis(refreshExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshTokenResponse generateNewToken(RefreshTokenRequest request) {
        User user = refreshTokenRepository.findByToken(request.getRefreshToken())
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .orElseThrow(() -> new InvalidBearerTokenException(request.getRefreshToken() + "Refresh token does not exist"));

        String token = jwtService.generateToken(user.getUsername(), user.getRole().toString());
        return RefreshTokenResponse.builder()
                .token(token)
                .refreshToken(request.getRefreshToken())
                .tokenType(OAuth2AccessToken.TokenType.BEARER.getValue())
                .build();
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(token);
            throw new InvalidBearerTokenException(token.getToken() + " Refresh token is expired. Please make a new login..!");
        }
        return token;
    }
}

