package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.RatingDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RatingService {
    RatingDTO createRatingByBookIdAndUserId(Integer bookId, String userEmail, RatingDTO ratingDTO);

    List<RatingDTO> getRatingsByBookId(Integer bookId);
}
