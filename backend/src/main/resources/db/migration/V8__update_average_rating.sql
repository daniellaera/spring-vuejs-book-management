-- Update each book's average_rating based on the ratings in the _rating table
-- executed after _rating table creation
UPDATE _book
SET average_rating = subquery.avg_score
    FROM (
    SELECT book_id, AVG(score) AS avg_score
    FROM _rating
    GROUP BY book_id
) AS subquery
WHERE _book.id = subquery.book_id;