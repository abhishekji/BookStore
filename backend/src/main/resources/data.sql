INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'Clean Code', 'Robert C. Martin', '9780132350884', 39.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780132350884');

INSERT INTO inventory (book_id, stock_quantity)
SELECT id, 8 FROM book b WHERE b.isbn = '9780132350884'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'The Pragmatic Programmer', 'Andrew Hunt and David Thomas', '9780135957059', 49.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780135957059');

INSERT INTO inventory (book_id, stock_quantity)
SELECT id, 5 FROM book b WHERE b.isbn = '9780135957059'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'Domain-Driven Design', 'Eric Evans', '9780321125217', 59.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780321125217');

INSERT INTO inventory (book_id, stock_quantity)
SELECT id, 0 FROM book b WHERE b.isbn = '9780321125217'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
