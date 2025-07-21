package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.dao.RatingDTO;
import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.Rating;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.RatingRepository;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.RatingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Autowired
    public RatingServiceImpl(RatingRepository ratingRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public RatingDTO createRatingByBookIdAndUserId(Integer bookId, String userEmail, RatingDTO ratingDTO) {
        log.info("Loading user by username: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", userEmail);
                    return new UsernameNotFoundException("User not found with email: " + userEmail);
                });

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.error("Book not found with ID: {}", bookId);
                    return new EntityNotFoundException("Book not found with ID: " + bookId);
                });

        Rating rating = new Rating();
        rating.setUser(user);
        rating.setBook(book);
        rating.setScore(ratingDTO.getScore());

        Rating savedRating = ratingRepository.save(rating);

        // update the book average
        if (book.getRatings().size() >= 2) {
            log.info("Updating average rating: {}", ratingDTO);
            this.updateAverageRating(book);
        }

        return convertRatingToRatingDTO(savedRating);

    }

    @Override
    public List<RatingDTO> getRatingsByBookId(Integer bookId) {
        List<Rating> ratingList = ratingRepository.findAllByBookId(bookId);

        return ratingList
                .stream()
                .map(this::convertRatingToRatingDTO)
                .collect(Collectors.toList());
    }

    private RatingDTO convertRatingToRatingDTO(Rating savedRating) {
        RatingDTO ratingDTO = new RatingDTO();
        ratingDTO.setScore(savedRating.getScore());
        ratingDTO.setUserId(savedRating.getUser().getId());
        return ratingDTO;
    }

    private void updateAverageRating(Book book) {
        List<Rating> ratings = book.getRatings();
        double averageRating = calculateAverageRating(ratings);
        book.setAverageRating(averageRating);
        bookRepository.save(book);
    }

    private Double calculateAverageRating(List<Rating> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            return 0.0; // Return 0 if no ratings
        }

        double sum = 0.0;
        int count = 0;

        for (Rating rating : ratings) {
            sum += Math.min(rating.getScore(), 5); // Cap each rating at 5 stars
            count++;
        }

        double average = sum / count; // Calculate average
        return Math.min(average * 2, 5.0); // Return capped average, max 5 stars
    }
}
