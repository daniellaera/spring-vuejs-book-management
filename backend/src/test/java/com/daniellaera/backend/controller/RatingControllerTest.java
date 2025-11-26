package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.RatingDTO;
import com.daniellaera.backend.service.JwtService;
import com.daniellaera.backend.service.RatingService;
import com.daniellaera.backend.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
//@AutoConfigureMockMvc(addFilters = false)
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RatingService ratingService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void createRating_Success() throws Exception {
        RatingDTO ratingDTO = new RatingDTO();
        ratingDTO.setScore(5);
        ratingDTO.setUserId(1); // Mock user ID

        when(ratingService.createRatingByBookIdAndUserId(anyInt(), any(String.class), any(RatingDTO.class)))
                .thenReturn(ratingDTO);

        String requestBody = """
                {
                  "score": 5,
                  "userId": 1
                }
                """;

        mockMvc.perform(post("/api/v3/rating/{bookId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()) // Include CSRF token
                        .with(SecurityMockMvcRequestPostProcessors.user("john.doe@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(5))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void createRating_Unauthenticated() throws Exception {
        RatingDTO ratingDTO = new RatingDTO();
        ratingDTO.setScore(5);
        ratingDTO.setUserId(1); // Mock user ID

        when(ratingService.createRatingByBookIdAndUserId(anyInt(), any(String.class), any(RatingDTO.class)))
                .thenReturn(ratingDTO);

        String requestBody = """
                {
                  "score": 5,
                  "userId": 1
                }
                """;

        mockMvc.perform(post("/api/v3/rating/{bookId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}