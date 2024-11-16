package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.CommentDTO;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {

    @InjectMocks
    private CommentController commentController; // Inject the controller to test

    private MockMvc mockMvc; // MockMvc for sending requests and checking results

    @Mock
    private CommentService commentService; // Mock the CommentService to simulate service layer behavior

    @Mock
    private User currentUser;

    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build(); // Setup MockMvc with the controller
    }

    @Test
    void getCommentsByBookId_ReturnsComments() throws Exception {
        // Arrange - Setup mock data for comments and user
        CommentDTO commentDTO1 = new CommentDTO();
        commentDTO1.setContent("This is a great book!");

        CommentDTO commentDTO2 = new CommentDTO();
        commentDTO2.setContent("I loved this book!");

        List<CommentDTO> commentDTOList = Arrays.asList(commentDTO1, commentDTO2); // List of comments

        // Mock the service method to return the list of comments
        given(commentService.getCommentsByBookId(anyInt())).willReturn(commentDTOList);

        // Act & Assert - Send GET request and check response
        mockMvc.perform(get("/api/v3/comment/{bookId}/comments", 1) // Send GET request with a bookId
                        .accept(MediaType.APPLICATION_JSON)) // Expecting JSON response
                .andExpect(status().isOk()) // Status code 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Content type JSON
                .andExpect(jsonPath("$.length()").value(2)) // Verify that two comments are returned
                .andExpect(jsonPath("$[0].content").value("This is a great book!")) // Check first comment content
                .andExpect(jsonPath("$[1].content").value("I loved this book!")); // Check second comment content

        verify(commentService, times(1)).getCommentsByBookId(1);
    }

    @Test
    void createComment_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        // Arrange - Create mock CommentDTO
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("This is a great book!");

        // Act & Assert - Perform POST request without authenticated user
        mockMvc.perform(post("/api/v3/comment/{bookId}", 1) // POST request to /api/v3/comment/{bookId}
                        .contentType(MediaType.APPLICATION_JSON) // Set content type to JSON
                        .content("{ \"content\": \"This is a great book!\" }") // Request body with comment content
                        .principal(() -> null)) // Simulate unauthenticated user by passing null principal
                .andExpect(status().isUnauthorized()) // Expect status 401 Unauthorized
                .andExpect(content().string("")); // Response body is empty (no content)

        // Verify that the service method was not called, as the user is unauthenticated
        verify(commentService, times(0)).createCommentByBookIdAndUserId(anyInt(), anyString(), any(CommentDTO.class));
    }


}
