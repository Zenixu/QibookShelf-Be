-- Seed dummy untuk profil dev (V100). Data milik user demo & sinta (password: demo12345 / sinta12345).
-- Hash bcrypt di bawah dibuat khusus untuk dev, JANGAN dipakai di produksi.

INSERT INTO users (email, username, password_hash, full_name) VALUES
    ('demo@bookshelf.dev',  'demo',  '$2b$10$AClYfnlvKSLAo64p5T6W9uq0A/QSQn/Q9lPrPfhY6GtFuAYlM0E3G', 'Demo User'),
    ('sinta@bookshelf.dev', 'sinta', '$2b$10$eJgjBYcZk7M3KRTrBn1AX.Ji3MYgppoZFgP7OA8ARxCQkLL1nt4rW', 'Sinta');

INSERT INTO publishers (name, city) VALUES
    ('Bentang Pustaka',        'Yogyakarta'),
    ('Lentera Dipantara',      'Jakarta'),
    ('Harper',                 'New York'),
    ('Addison-Wesley',         'Boston'),
    ('Gramedia Pustaka Utama', 'Jakarta');

INSERT INTO authors (name, nationality) VALUES
    ('Andrea Hirata',        'Indonesia'),
    ('Pramoedya Ananta Toer','Indonesia'),
    ('Yuval Noah Harari',    'Israel'),
    ('Andrew Hunt',          'Amerika Serikat'),
    ('David Thomas',         'Inggris'),
    ('James Clear',          'Amerika Serikat');

INSERT INTO categories (name, slug) VALUES
    ('Fiksi',             'fiksi'),
    ('Sejarah',           'sejarah'),
    ('Teknologi',         'teknologi'),
    ('Sastra',            'sastra'),
    ('Pengembangan Diri', 'pengembangan-diri');

INSERT INTO books (title, isbn, publish_year, publisher_id, user_id) VALUES
    ('Laskar Pelangi',            '9789793062792', 2005, (SELECT id FROM publishers WHERE name = 'Bentang Pustaka'),        (SELECT id FROM users WHERE username = 'demo')),
    ('Bumi Manusia',              '9789799731234', 2005, (SELECT id FROM publishers WHERE name = 'Lentera Dipantara'),      (SELECT id FROM users WHERE username = 'demo')),
    ('Sapiens',                   '9780062316097', 2015, (SELECT id FROM publishers WHERE name = 'Harper'),                 (SELECT id FROM users WHERE username = 'demo')),
    ('The Pragmatic Programmer',  '9780201616224', 1999, (SELECT id FROM publishers WHERE name = 'Addison-Wesley'),        (SELECT id FROM users WHERE username = 'demo')),
    ('Atomic Habits',             '9786020633176', 2019, (SELECT id FROM publishers WHERE name = 'Gramedia Pustaka Utama'), (SELECT id FROM users WHERE username = 'sinta'));

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b JOIN authors a ON (b.title, a.name) IN (
    ('Laskar Pelangi',           'Andrea Hirata'),
    ('Bumi Manusia',             'Pramoedya Ananta Toer'),
    ('Sapiens',                  'Yuval Noah Harari'),
    ('The Pragmatic Programmer', 'Andrew Hunt'),
    ('The Pragmatic Programmer', 'David Thomas'),
    ('Atomic Habits',            'James Clear')
);

INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id FROM books b JOIN categories c ON (b.title, c.slug) IN (
    ('Laskar Pelangi',           'fiksi'),
    ('Laskar Pelangi',           'sastra'),
    ('Bumi Manusia',             'fiksi'),
    ('Bumi Manusia',             'sejarah'),
    ('Sapiens',                  'sejarah'),
    ('The Pragmatic Programmer', 'teknologi'),
    ('Atomic Habits',            'pengembangan-diri')
);

INSERT INTO reading_logs (book_id, user_id, status, started_at, finished_at, rating)
SELECT b.id, u.id, v.status, v.started_at, v.finished_at, v.rating
FROM books b
CROSS JOIN users u
JOIN (VALUES
    ('Laskar Pelangi',           'demo',  'DONE',     DATE '2026-01-05', DATE '2026-01-20', 5),
    ('Laskar Pelangi',           'demo',  'DONE',     DATE '2026-06-01', DATE '2026-06-10', 4),
    ('Sapiens',                  'demo',  'READING',  DATE '2026-09-15', NULL,              NULL),
    ('The Pragmatic Programmer', 'demo',  'WISHLIST', NULL,              NULL,              NULL),
    ('Atomic Habits',            'sinta', 'WISHLIST', NULL,              NULL,              NULL)
) AS v(title, username, status, started_at, finished_at, rating)
  ON v.title = b.title AND v.username = u.username;
