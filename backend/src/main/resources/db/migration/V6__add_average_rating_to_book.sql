-- Add the average_rating column to the _book table
ALTER TABLE _book
    ADD COLUMN average_rating DOUBLE PRECISION DEFAULT 0;
