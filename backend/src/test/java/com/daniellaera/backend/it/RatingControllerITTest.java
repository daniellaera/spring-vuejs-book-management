package com.daniellaera.backend.it;

import com.daniellaera.backend.dao.RatingDTO;
import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.Comment;
import com.daniellaera.backend.model.Role;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.CommentRepository;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.JwtService;
import com.daniellaera.backend.service.RatingService;
import com.daniellaera.backend.utils.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RatingControllerITTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RatingService ratingService;

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService; // Inject JwtService to generate the JWT

    private Book book;
    private User user;

    @BeforeEach
    void setup() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll(); // Ensure all users are deleted

        user = new User();
        user.setEmail("john.doe@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        userRepository.save(user); // This will persist a single user with consistent ID

        book = new Book();
        book.setTitle("Test Book");
        book.setDescription("A book for testing comments.");
        book.setIsbn("978-1234567890");
        book.setAuthor("Thomas H. Cormen");
        book.setGenre("Fiction");
        // Use LocalDate and convert to Date
        LocalDate publishedLocalDate = LocalDate.of(2020, 5, 15); // May 15, 2020
        book.setPublishedDate(Date.from(publishedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        book.setCreatedDate(new Date()); // Current date and time
        book.setCreatedBy(user);
        bookRepository.save(book);

        Comment comment1 = new Comment();
        comment1.setContent("Great book!");
        comment1.setBook(book);
        comment1.setUser(user);

        Comment comment2 = new Comment();
        comment2.setContent("I enjoyed it.");
        comment2.setBook(book);
        comment2.setUser(user);

        commentRepository.saveAll(Arrays.asList(comment1, comment2));

        assertNotNull(book.getId());
    }

    @Test
    //@WithMockUser(username = "john.doe@example.com", roles = "USER")
    void createRating_Success() throws Exception {
        String userEmail = "john.doe@example.com";
        String jwtToken = jwtService.generateToken(userEmail, "USER");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        RatingDTO ratingDTO = new RatingDTO();
        ratingDTO.setScore(5);
        ratingDTO.setUserId(user.getId()); // Mock user ID

        // Mock service behavior
        when(ratingService.createRatingByBookIdAndUserId(anyInt(), any(String.class), any(RatingDTO.class)))
                .thenReturn(ratingDTO);

        String requestBody = """
                {
                  "score": 5,
                  "userId": 1
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v3/rating/{bookId}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(5));
    }
}