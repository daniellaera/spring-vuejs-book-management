-- Create the Rating table
CREATE TABLE _rating
(
    id      INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Auto-increment ID
    score   INT NOT NULL CHECK (score BETWEEN 1 AND 5),           -- Ensure the score is between 1 and 5
    book_id INT NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT fk_rating_book FOREIGN KEY (book_id) REFERENCES _book (id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES _user (id) ON DELETE CASCADE
);

-- Insert initial ratings for books by different users
INSERT INTO _rating (score, book_id, user_id)
VALUES (5, 1, 1), -- User 1 rates Book 1 (Spring Boot Essentials) as 5
       (4, 1, 2), -- User 2 rates Book 1 as 4
       (3, 2, 1), -- User 1 rates Book 2 (Clean Code) as 3
       (5, 2, 3), -- User 3 rates Book 2 as 5
       (4, 3, 2), -- User 2 rates Book 3 (Effective Java) as 4
       (5, 3, 4), -- User 4 rates Book 3 as 5
       (2, 4, 3), -- User 3 rates Book 4 (The Pragmatic Programmer) as 2
       (4, 5, 1), -- User 1 rates Book 5 (To Kill a Mockingbird) as 4
       (5, 6, 2), -- User 2 rates Book 6 (1984) as 5
       (3, 7, 3), -- User 3 rates Book 7 (The Great Gatsby) as 3
       (4, 8, 4), -- User 4 rates Book 8 (Introduction to Algorithms) as 4
       (5, 9, 1), -- User 1 rates Book 9 (Artificial Intelligence: A Modern Approach) as 5
       (4, 10, 2); -- User 2 rates Book 10 (The Alchemist) as 4