-- Ensure the `book_id` foreign key constraint is present on `_borrow`
ALTER TABLE _borrow
DROP
CONSTRAINT IF EXISTS fk_borrow_book;

ALTER TABLE _borrow
    ADD CONSTRAINT fk_borrow_book
        FOREIGN KEY (book_id) REFERENCES _book (id) ON DELETE CASCADE;

-- Ensure the `user_id` foreign key constraint is present on `_borrow`
ALTER TABLE _borrow
DROP
CONSTRAINT IF EXISTS fk_borrow_user;

ALTER TABLE _borrow
    ADD CONSTRAINT fk_borrow_user
        FOREIGN KEY (user_id) REFERENCES _user (id) ON DELETE CASCADE;

-- Remove the unique constraint between `book_id` and `user_id`
ALTER TABLE _borrow
DROP
CONSTRAINT IF EXISTS fk_borrow_book_user;