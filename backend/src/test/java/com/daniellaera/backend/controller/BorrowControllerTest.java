package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.BorrowDTO;
import com.daniellaera.backend.service.BorrowService;
import com.daniellaera.backend.service.JwtService;
import com.daniellaera.backend.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = BorrowController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BorrowService borrowService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().authenticated()
                    )
                    // ADD THIS: This enables the standard 401 challenge behavior
                    .httpBasic(withDefaults());

            return http.build();
        }
    }

    @Test
    void createRating_Success() throws Exception {
        BorrowDTO borrowDTO = BorrowDTO.builder()
                .bookId(2)
                .userId(1)
                .borrowStartDate(new Date())
                .borrowEndDate(new Date())
                .isReturned(false)
                .build();

        when(borrowService.createBorrowByBookIdAndUserId(anyInt(), any(String.class), any(BorrowDTO.class)))
                .thenReturn(borrowDTO);

        String requestBody = """
        {
          "bookId": 2,
          "userId": 1,
          "borrowStartDate": "2025-12-01T10:00:00.000Z",
          "borrowEndDate": "2025-12-31T10:00:00.000Z"
        }
        """;

        mockMvc.perform(post("/api/v3/borrow/{bookId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        // Removed .with(csrf()) because we disabled it in TestSecurityConfig
                        //.with(csrf()) // Include CSRF token
                        .with(SecurityMockMvcRequestPostProcessors.user("john.doe@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(2))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.borrowStartDate").isNotEmpty())
                .andExpect(jsonPath("$.borrowEndDate").isNotEmpty());
    }

    @Test
    void createRating_Unauthenticated() throws Exception {
        BorrowDTO borrowDTO = BorrowDTO.builder()
                .bookId(1)
                .userId(1)
                .borrowStartDate(new Date())
                .borrowEndDate(new Date())
                .isReturned(false)
                .build();

        when(borrowService.createBorrowByBookIdAndUserId(anyInt(), any(String.class), any(BorrowDTO.class)))
                .thenReturn(borrowDTO);

        String requestBody = """
                {
                  "score": 5,
                  "userId": 1
                }
                """;

        mockMvc.perform(post("/api/v3/borrow/{bookId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}