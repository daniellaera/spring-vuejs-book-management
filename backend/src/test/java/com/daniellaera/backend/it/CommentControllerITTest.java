package com.daniellaera.backend.it;

import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.Comment;
import com.daniellaera.backend.model.Role;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.CommentRepository;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.JwtService;
import com.daniellaera.backend.utils.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CommentControllerITTest {

    @Autowired
    private MockMvc mockMvc;

    // ✔ ONLY mock authenticationManager (NOT CommentService)
    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Autowired private CommentRepository commentRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;

    private Book book;
    private User user;

    @BeforeEach
    void setup() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setEmail("john.doe@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        userRepository.save(user);

        book = new Book();
        book.setTitle("Test Book");
        book.setDescription("A book for testing comments.");
        book.setIsbn("978-1234567890");
        book.setAuthor("Thomas H. Cormen");
        book.setGenre("Fiction");

        LocalDate publishedLocalDate = LocalDate.of(2020, 5, 15);
        book.setPublishedDate(Date.from(publishedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        book.setCreatedDate(new Date());
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
    void getCommentsByBookId_ReturnsComments() throws Exception {

        mockMvc.perform(get("/api/v3/comment/{bookId}/comments", book.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("Great book!"))
                .andExpect(jsonPath("$[1].content").value("I enjoyed it."));
    }

    @Test
    void createComment_ReturnsCreatedComment() throws Exception {

        String newCommentJson = """
                {
                    "content": "Another insightful comment."
                }
                """;

        // Create a JWT token for the integration test
        String jwtToken = jwtService.generateToken("john.doe@example.com", "USER");

        // Mock the AuthenticationManager ONLY (required for Spring Security)
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        // Perform POST request
        mockMvc.perform(post("/api/v3/comment/{bookId}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCommentJson)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Another insightful comment."));

        // ✔ Verify it was actually saved in DB
        assertEquals(3, commentRepository.count());
    }

    @Test
    void getCommentsByBookId_WhenNoComments_ReturnsEmptyArray() throws Exception {

        commentRepository.deleteAll();

        mockMvc.perform(get("/api/v3/comment/{bookId}/comments", book.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}