CREATE TABLE _borrow
(
    id                INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Auto-increment ID
    borrow_start_date DATE    NOT NULL,
    borrow_end_date   DATE    NOT NULL,
    is_returned       BOOLEAN NOT NULL DEFAULT FALSE, -- Tracks if the book has been returned
    book_id           INT     NOT NULL,
    user_id           INT     NOT NULL,
    CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES _book (id) ON DELETE CASCADE,
    CONSTRAINT fk_borrow_user FOREIGN KEY (user_id) REFERENCES _user (id) ON DELETE CASCADE,
    UNIQUE (book_id, user_id) -- Ensure unique book-user combination for borrow
);