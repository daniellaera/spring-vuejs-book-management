package com.daniellaera.backend.repository.impl;

import com.daniellaera.backend.dao.*;
import com.daniellaera.backend.repository.BookRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BookRepositoryCustomImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Page<BookDTO> findAllBooksOptimized(Pageable pageable, String search) {
        List<Object> params = new ArrayList<>();

        // WHERE clause
        String whereClause = "";
        if (search != null && !search.trim().isEmpty()) {
            whereClause = " WHERE LOWER(b.title) LIKE ? OR LOWER(b.author) LIKE ?";
            String pattern = "%" + search.toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        // Count
        String countSql = "SELECT COUNT(*) FROM _book b" + whereClause;
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());

        // Sort - MAP JAVA FIELD NAMES TO SQL COLUMN NAMES
        String orderBy = " ORDER BY ";
        if (pageable.getSort().isSorted()) {
            orderBy += pageable.getSort().stream()
                    .map(order -> {
                        String field = order.getProperty();
                        // Map camelCase to snake_case
                        String column = switch (field) {
                            case "createdDate" -> "created_date";
                            case "publishedDate" -> "published_date";
                            case "averageRating" -> "average_rating";
                            case "isAvailable" -> "is_available";
                            default -> field; // title, author, isbn, genre stay the same
                        };
                        return "b." + column + (order.isAscending() ? " ASC" : " DESC");
                    })
                    .collect(Collectors.joining(", "));
        } else {
            orderBy += "b.created_date DESC";
        }

        // Query
        String sql = """
        SELECT b.id, b.title, b.description, b.author, b.isbn, 
               b.is_available, b.published_date, b.average_rating, 
               b.genre, b.created_date
        FROM _book b
        """ + whereClause + orderBy + " LIMIT ? OFFSET ?";

        params.add(pageable.getPageSize());
        params.add(pageable.getPageNumber() * pageable.getPageSize());

        List<BookDTO> books = jdbcTemplate.query(sql,
                (rs, rowNum) -> BookDTO.builder()
                        .id(rs.getInt("id"))
                        .title(rs.getString("title"))
                        .description(rs.getString("description"))
                        .author(rs.getString("author"))
                        .isbn(rs.getString("isbn"))
                        .isAvailable(rs.getBoolean("is_available"))
                        .publishedDate(rs.getDate("published_date"))
                        .averageRating(rs.getDouble("average_rating"))
                        .genre(rs.getString("genre"))
                        .createdDate(rs.getDate("created_date"))
                        .build(),
                params.toArray()
        );

        return new PageImpl<>(books, pageable, total != null ? total : 0);
    }

    @Override
    public Optional<BookDTO> findBookByIdOptimized(Integer bookId) {
        String sql = """
        SELECT
            b.id,
            b.title,
            b.description,
            b.author,
            b.isbn,
            b.is_available,
            b.published_date,
            b.average_rating,
            b.genre,
            b.created_date,
            u.id as user_id,
            u.first_name as user_first_name,
            u.last_name as user_last_name,
            cu.first_name as comment_user_first_name,
            cu.last_name as comment_user_last_name,
            c.content as comment_content,
            r.score as rating_score,
            r.user_id as rating_user_id,
            bor.book_id as borrow_book_id,
            bor.user_id as borrow_user_id,
            bor.is_returned as borrow_is_returned,
            bor.borrow_start_date as borrow_start_date,
            bor.borrow_end_date as borrow_end_date
        FROM _book b
        LEFT JOIN _user u ON b.created_by = u.id
        LEFT JOIN _comment c ON c.book_id = b.id
        LEFT JOIN _user cu ON c.user_id = cu.id
        LEFT JOIN _rating r ON r.book_id = b.id
        LEFT JOIN _borrow bor ON bor.book_id = b.id AND bor.is_returned = false
        WHERE b.id = ?
        """;

        try {
            BookDTO book = jdbcTemplate.query(sql, rs -> {
                if (!rs.next()) {
                    return null;
                }

                BookDTO.BookDTOBuilder builder = BookDTO.builder()
                        .id(rs.getInt("id"))
                        .title(rs.getString("title"))
                        .description(rs.getString("description"))
                        .author(rs.getString("author"))
                        .isbn(rs.getString("isbn"))
                        .isAvailable(rs.getBoolean("is_available"))
                        .publishedDate(rs.getDate("published_date"))
                        .averageRating(rs.getDouble("average_rating"))
                        .genre(rs.getString("genre"))
                        .createdDate(rs.getDate("created_date"));

                // User DTO (book creator)
                Integer userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    String firstName = rs.getString("user_first_name");
                    String lastName = rs.getString("user_last_name");

                    builder.userDTO(UserDTO.builder()
                            .id(userId)
                            .fullName(firstName + " " + lastName)
                            .build());
                }

                List<CommentDTO> comments = new ArrayList<>();
                List<RatingDTO> ratings = new ArrayList<>();
                BorrowDTO borrow = null;
                Set<String> addedComments = new HashSet<>();
                Set<Integer> addedRatings = new HashSet<>();

                do {
                    // Comments - JOIN with user to get name
                    String commentContent = rs.getString("comment_content");
                    if (commentContent != null) {
                        String commentFirstName = rs.getString("comment_user_first_name");
                        String commentLastName = rs.getString("comment_user_last_name");

                        if (commentFirstName != null && commentLastName != null) {
                            String authorFullName = commentFirstName + " " + commentLastName;
                            String commentKey = authorFullName + "||" + commentContent;

                            if (!addedComments.contains(commentKey)) {
                                comments.add(CommentDTO.builder()
                                        .authorFullName(authorFullName)
                                        .content(commentContent)
                                        .build());
                                addedComments.add(commentKey);
                            }
                        }
                    }

                    // Ratings
                    Integer ratingUserId = rs.getInt("rating_user_id");
                    if (!rs.wasNull() && !addedRatings.contains(ratingUserId)) {
                        ratings.add(RatingDTO.builder()
                                .score(rs.getInt("rating_score"))
                                .userId(ratingUserId)
                                .build());
                        addedRatings.add(ratingUserId);
                    }

                    // Borrow
                    if (borrow == null) {
                        Integer borrowBookId = rs.getInt("borrow_book_id");
                        if (!rs.wasNull()) {
                            borrow = BorrowDTO.builder()
                                    .bookId(borrowBookId)
                                    .userId(rs.getInt("borrow_user_id"))
                                    .isReturned(rs.getBoolean("borrow_is_returned"))
                                    .borrowStartDate(rs.getDate("borrow_start_date"))
                                    .borrowEndDate(rs.getDate("borrow_end_date"))
                                    .build();
                        }
                    }
                } while (rs.next());

                return builder
                        .comments(comments.isEmpty() ? null : comments)
                        .ratings(ratings.isEmpty() ? null : ratings)
                        .borrow(borrow)
                        .build();
            }, bookId);

            return Optional.ofNullable(book);

        } catch (Exception e) {
            log.error("Error fetching book by ID: {}", bookId, e);
            return Optional.empty();
        }
    }
}
