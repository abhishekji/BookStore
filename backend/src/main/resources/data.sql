INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'Clean Code', 'Robert C. Martin', '9780132350884', 39.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780132350884');

INSERT INTO inventory (id, book_id, stock_quantity)
SELECT RANDOM_UUID(), id, 8 FROM book b WHERE b.isbn = '9780132350884'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'The Pragmatic Programmer', 'Andrew Hunt and David Thomas', '9780135957059', 49.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780135957059');

INSERT INTO inventory (id, book_id, stock_quantity)
SELECT RANDOM_UUID(), id, 5 FROM book b WHERE b.isbn = '9780135957059'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'Domain-Driven Design', 'Eric Evans', '9780321125217', 59.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780321125217');

INSERT INTO inventory (id, book_id, stock_quantity)
SELECT RANDOM_UUID(), id, 0 FROM book b WHERE b.isbn = '9780321125217'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'Atomic Habits', 'James Clear', '9780735211292', 27.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780735211292');
INSERT INTO inventory (id, book_id, stock_quantity)
SELECT RANDOM_UUID(), id, 12 FROM book b WHERE b.isbn = '9780735211292'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'The Hobbit', 'J. R. R. Tolkien', '9780547928227', 18.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780547928227');
INSERT INTO inventory (id, book_id, stock_quantity)
SELECT RANDOM_UUID(), id, 9 FROM book b WHERE b.isbn = '9780547928227'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'Refactoring', 'Martin Fowler', '9780134757599', 44.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780134757599');
INSERT INTO inventory (id, book_id, stock_quantity)
SELECT RANDOM_UUID(), id, 6 FROM book b WHERE b.isbn = '9780134757599'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'Design Patterns', 'Erich Gamma', '9780201633610', 54.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780201633610');
INSERT INTO inventory (id, book_id, stock_quantity)
SELECT RANDOM_UUID(), id, 4 FROM book b WHERE b.isbn = '9780201633610'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'Effective Java', 'Joshua Bloch', '9780134685991', 46.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780134685991');
INSERT INTO inventory (id, book_id, stock_quantity)
SELECT RANDOM_UUID(), id, 7 FROM book b WHERE b.isbn = '9780134685991'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'The Psychology of Money', 'Morgan Housel', '9780857197689', 22.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780857197689');
INSERT INTO inventory (id, book_id, stock_quantity)
SELECT RANDOM_UUID(), id, 10 FROM book b WHERE b.isbn = '9780857197689'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price)
SELECT RANDOM_UUID(), 'The Alchemist', 'Paulo Coelho', '9780062315007', 16.99
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780062315007');
INSERT INTO inventory (id, book_id, stock_quantity)
SELECT RANDOM_UUID(), id, 11 FROM book b WHERE b.isbn = '9780062315007'
  AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);

INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), '1984', 'George Orwell', '9780451524935', 14.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780451524935');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 10 FROM book b WHERE b.isbn = '9780451524935' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'To Kill a Mockingbird', 'Harper Lee', '9780061120084', 15.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780061120084');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 8 FROM book b WHERE b.isbn = '9780061120084' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'Pride and Prejudice', 'Jane Austen', '9780141439518', 12.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780141439518');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 9 FROM book b WHERE b.isbn = '9780141439518' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', 13.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780743273565');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 7 FROM book b WHERE b.isbn = '9780743273565' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'Moby-Dick', 'Herman Melville', '9781503280786', 17.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9781503280786');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 6 FROM book b WHERE b.isbn = '9781503280786' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'The Catcher in the Rye', 'J. D. Salinger', '9780316769488', 14.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780316769488');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 8 FROM book b WHERE b.isbn = '9780316769488' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'Sapiens', 'Yuval Noah Harari', '9780062316097', 24.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780062316097');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 10 FROM book b WHERE b.isbn = '9780062316097' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'Educated', 'Tara Westover', '9780399590504', 19.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780399590504');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 8 FROM book b WHERE b.isbn = '9780399590504' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'Dune', 'Frank Herbert', '9780441172719', 18.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780441172719');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 9 FROM book b WHERE b.isbn = '9780441172719' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'The Book Thief', 'Markus Zusak', '9780375842207', 16.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780375842207');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 7 FROM book b WHERE b.isbn = '9780375842207' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'Little Women', 'Louisa May Alcott', '9780147514011', 13.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780147514011');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 8 FROM book b WHERE b.isbn = '9780147514011' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'The Kite Runner', 'Khaled Hosseini', '9781594631931', 17.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9781594631931');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 6 FROM book b WHERE b.isbn = '9781594631931' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'A Brief History of Time', 'Stephen Hawking', '9780553380163', 20.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780553380163');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 5 FROM book b WHERE b.isbn = '9780553380163' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'Thinking, Fast and Slow', 'Daniel Kahneman', '9780374533557', 21.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780374533557');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 7 FROM book b WHERE b.isbn = '9780374533557' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'The Lean Startup', 'Eric Ries', '9780307887894', 18.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780307887894');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 8 FROM book b WHERE b.isbn = '9780307887894' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'The 7 Habits of Highly Effective People', 'Stephen Covey', '9781982137274', 19.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9781982137274');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 9 FROM book b WHERE b.isbn = '9781982137274' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'Good to Great', 'Jim Collins', '9780066620992', 23.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780066620992');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 6 FROM book b WHERE b.isbn = '9780066620992' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'Man''s Search for Meaning', 'Viktor Frankl', '9780807014295', 15.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780807014295');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 8 FROM book b WHERE b.isbn = '9780807014295' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'The Power of Now', 'Eckhart Tolle', '9781577314806', 17.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9781577314806');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 7 FROM book b WHERE b.isbn = '9781577314806' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'The Midnight Library', 'Matt Haig', '9780525559474', 18.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780525559474');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 10 FROM book b WHERE b.isbn = '9780525559474' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'Project Hail Mary', 'Andy Weir', '9780593135204', 21.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780593135204');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 9 FROM book b WHERE b.isbn = '9780593135204' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
INSERT INTO book (id, title, author, isbn, price) SELECT RANDOM_UUID(), 'The Martian', 'Andy Weir', '9780553418026', 16.99 WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9780553418026');
INSERT INTO inventory (id, book_id, stock_quantity) SELECT RANDOM_UUID(), id, 8 FROM book b WHERE b.isbn = '9780553418026' AND NOT EXISTS (SELECT 1 FROM inventory i WHERE i.book_id = b.id);
