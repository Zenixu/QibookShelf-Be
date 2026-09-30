INSERT INTO publishers (name, city) VALUES
    ('Bentang Pustaka',        'Yogyakarta'),
    ('Lentera Dipantara',      'Jakarta'),
    ('Harper',                 'New York'),
    ('Addison-Wesley',         'Boston');

INSERT INTO authors (name, nationality) VALUES
    ('Andrea Hirata',        'Indonesia'),
    ('Pramoedya Ananta Toer','Indonesia'),
    ('Yuval Noah Harari',    'Israel'),
    ('Andrew Hunt',          'Amerika Serikat'),
    ('David Thomas',         'Inggris');

INSERT INTO categories (name, slug) VALUES
    ('Fiksi',    'fiksi'),
    ('Sejarah',  'sejarah'),
    ('Teknologi','teknologi'),
    ('Sastra',   'sastra');

INSERT INTO books (title, isbn, publish_year, publisher_id) VALUES
    ('Laskar Pelangi',            '9789793062792', 2005, (SELECT id FROM publishers WHERE name = 'Bentang Pustaka')),
    ('Bumi Manusia',              '9789799731234', 2005, (SELECT id FROM publishers WHERE name = 'Lentera Dipantara')),
    ('Sapiens',                   '9780062316097', 2015, (SELECT id FROM publishers WHERE name = 'Harper')),
    ('The Pragmatic Programmer',  '9780201616224', 1999, (SELECT id FROM publishers WHERE name = 'Addison-Wesley'));

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b JOIN authors a ON (b.title, a.name) IN (
    ('Laskar Pelangi',           'Andrea Hirata'),
    ('Bumi Manusia',             'Pramoedya Ananta Toer'),
    ('Sapiens',                  'Yuval Noah Harari'),
    ('The Pragmatic Programmer', 'Andrew Hunt'),
    ('The Pragmatic Programmer', 'David Thomas')   -- ko-penulis
);

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b JOIN categories c ON (b.title, c.slug) IN (
    ('Laskar Pelangi',           'fiksi'),
    ('Laskar Pelangi',           'sastra'),
    ('Bumi Manusia',             'fiksi'),
    ('Bumi Manusia',             'sejarah'),
    ('Sapiens',                  'sejarah'),
    ('The Pragmatic Programmer', 'teknologi')
);

INSERT INTO reading_logs (book_id, status, started_at, finished_at, rating)
SELECT id, 'DONE',     DATE '2026-01-05', DATE '2026-01-20', 5    FROM books WHERE title = 'Laskar Pelangi' UNION ALL
SELECT id, 'DONE',     DATE '2026-06-01', DATE '2026-06-10', 4    FROM books WHERE title = 'Laskar Pelangi' UNION ALL  -- baca ulang
SELECT id, 'READING',  DATE '2026-09-15', NULL,              NULL FROM books WHERE title = 'Sapiens'        UNION ALL
SELECT id, 'WISHLIST', NULL,              NULL,              NULL FROM books WHERE title = 'The Pragmatic Programmer';
