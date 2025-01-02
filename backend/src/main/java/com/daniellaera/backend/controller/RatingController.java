package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.RatingDTO;
import com.daniellaera.backend.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v3/rating")
public class RatingController {

    private final RatingService ratingService;

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<List<RatingDTO>> getRatingsByBookId(@PathVariable Integer bookId) {
        List<RatingDTO> ratingDTOList = ratingService.getRatingsByBookId(bookId);
        return new ResponseEntity<>(ratingDTOList, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{bookId}")
    public ResponseEntity<RatingDTO> createRating(
            Authentication authentication, // Get the logged-in user
            @PathVariable Integer bookId,
            @RequestBody RatingDTO ratingDTO) {

        // Check if authentication is null or not authenticated
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String userEmail = authentication.getName();

        return ResponseEntity.ok(ratingService.createRatingByBookIdAndUserId(bookId, userEmail, ratingDTO));
    }
}
