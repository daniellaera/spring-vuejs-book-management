-- 1. Add the columns without the NOT NULL constraint yet
ALTER TABLE _book
    ADD created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE _book
    ADD published_date DATE;

-- 2. Populate all rows with default values to ensure no NULL values
UPDATE _book
SET published_date = CASE
                         WHEN genre = 'Technology' THEN '2015-01-01'::date
                         WHEN genre = 'Programming' THEN '2010-01-01'::date
                         WHEN genre = 'Fiction' THEN '2000-01-01'::date
                         ELSE '2000-01-01'::date
END;

UPDATE _book
SET created_date = CURRENT_TIMESTAMP;

-- 3. Alter the table to set NOT NULL constraints only after population
ALTER TABLE _book
    ALTER COLUMN published_date SET NOT NULL;

ALTER TABLE _book
    ALTER COLUMN created_date SET NOT NULL;