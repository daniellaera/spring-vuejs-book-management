-- Create the book table with genres
CREATE TABLE _book
(
    id          INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Auto-increment ID
    title       VARCHAR(255) NOT NULL,                                -- Short string for title
    isbn        VARCHAR(17)  NOT NULL,                                -- Standard length for ISBN
    description TEXT,                                                 -- Longer text for book description
    author      VARCHAR(255) NOT NULL,                                -- Author name as a string
    genre       VARCHAR(50) NOT NULL,                                 -- Genre of the book
    created_by  INTEGER NOT NULL,                                     -- New field for storing user reference
    CONSTRAINT fk_user FOREIGN KEY (created_by) REFERENCES _user (id) ON DELETE CASCADE -- Reference to user
);

-- Insert initial book data with diverse genres
INSERT INTO _book (title, isbn, description, author, genre, created_by)
VALUES ('Spring Boot Essentials', '978-1234567890', 'A comprehensive guide to mastering Spring Boot.', 'Craig Walls', 'Technology', 1),
       ('Clean Code', '978-0132350884', 'A handbook of agile software craftsmanship.', 'Robert C. Martin', 'Programming', 2),
       ('Effective Java', '978-0134685991', 'Best practices and tips for writing robust Java code.', 'Joshua Bloch', 'Programming', 3),
       ('The Pragmatic Programmer', '978-0201616224', 'Your journey to mastery, updated for modern development.', 'Andy Hunt & Dave Thomas', 'Programming' , 4),
       ('To Kill a Mockingbird', '978-0061120084', 'A novel of warmth and humor despite dealing with serious issues.', 'Harper Lee', 'Fiction', 1),
       ('1984', '978-0451524935', 'A dystopian novel depicting a totalitarian regime.', 'George Orwell', 'Fiction', 2),
       ('The Great Gatsby', '978-0743273565', 'A story of the Jazz Age and the American dream.', 'F. Scott Fitzgerald', 'Fiction', 3),
       ('Introduction to Algorithms', '978-0262033848', 'Comprehensive coverage of modern algorithms.', 'Thomas H. Cormen', 'Technology', 4),
       ('Artificial Intelligence: A Modern Approach', '978-0136042594', 'The most comprehensive, up-to-date introduction to AI.', 'Stuart Russell & Peter Norvig', 'Technology', 1),
       ('The Alchemist', '978-0061122415', 'A philosophical novel about pursuing your dreams.', 'Paulo Coelho', 'Fiction', 2);