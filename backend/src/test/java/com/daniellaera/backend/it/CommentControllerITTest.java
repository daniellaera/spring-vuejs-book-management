package com.daniellaera.backend.it;

import com.daniellaera.backend.dao.CommentDTO;
import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.Comment;
import com.daniellaera.backend.model.Role;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.CommentRepository;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.CommentService;
import com.daniellaera.backend.service.JwtService;
import com.daniellaera.backend.utils.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CommentService commentService;

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
        // Arrange: Mock comment creation
        CommentDTO newComment = new CommentDTO();
        newComment.setContent("Another insightful comment.");
        String newCommentJson = """
                {
                    "content": "Another insightful comment."
                }
                """;

        String userEmail = "john.doe@example.com";
        String jwtToken = jwtService.generateToken(userEmail, "USER");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);

        // Mock authenticationManager to return a valid authentication object
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        // Mock the service to return the newly created comment
        when(commentService.createCommentByBookIdAndUserId(any(Integer.class), any(String.class), any(CommentDTO.class)))
                .thenReturn(newComment);

        // Act: Perform the POST request with JWT token in the Authorization header
        mockMvc.perform(post("/api/v3/comment/{bookId}", book.getId())
                        .contentType("application/json")
                        .accept("application/json")
                        .content(newCommentJson)
                        .header("Authorization", "Bearer " + jwtToken)) // Mock JWT token
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content").value("Another insightful comment."));
    }

    @Test
    void getCommentsByBookId_WhenNoComments_ReturnsEmptyArray() throws Exception {
        commentRepository.deleteAll();

        mockMvc.perform(get("/api/v3/comment/{bookId}/comments", book.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }
}
