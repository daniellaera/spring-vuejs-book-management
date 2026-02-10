CREATE INDEX idx_book_title ON _book(title);
CREATE INDEX idx_book_author ON _book(author);
CREATE UNIQUE INDEX idx_book_isbn ON _book(isbn);
CREATE INDEX idx_book_is_available ON _book(is_available);
CREATE INDEX idx_book_title_author ON _book(title, author);