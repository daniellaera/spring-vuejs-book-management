package com.daniellaera.backend.repository;

import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.Rating;
import com.daniellaera.backend.model.Role;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.utils.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RatingRepositoryTest {

    @Autowired
    RatingRepository ratingRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserRepository userRepository;

    private Book testBook;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        userRepository.deleteAll();

        User testUser = new User();
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        // Create a test book
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("978-1234567890");
        testBook.setAuthor("Thomas H. Cormen");
        testBook.setGenre("Fiction");
        testBook.setCreatedBy(testUser);

        LocalDate publishedLocalDate = LocalDate.of(2020, 5, 15); // May 15, 2020
        testBook.setPublishedDate(Date.from(publishedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        testBook.setCreatedDate(new Date());

        bookRepository.save(testBook);

        // Add ratings to the test book
        Rating rating1 = new Rating();
        rating1.setScore(4);
        rating1.setUser(testUser);
        rating1.setBook(testBook);

        Rating rating2 = new Rating();
        rating2.setScore(5);
        rating2.setUser(testUser);
        rating2.setBook(testBook);

        ratingRepository.saveAll(List.of(rating1, rating2));
    }

    @Test
    void shouldFindAllRatingsByBookId() {
        // Fetch ratings by book ID
        List<Rating> ratings = ratingRepository.findAllByBookId(testBook.getId());

        // Assert the fetched ratings
        assertThat(ratings).hasSize(2);
        assertThat(ratings.get(0).getScore()).isEqualTo(4);
        assertThat(ratings.get(1).getScore()).isEqualTo(5);
        assertThat(ratings.get(0).getBook().getId()).isEqualTo(testBook.getId());
    }

}
