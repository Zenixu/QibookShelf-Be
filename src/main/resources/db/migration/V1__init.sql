-- ============ MASTER ============
CREATE TABLE authors (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    nationality VARCHAR(80)
);

CREATE TABLE publishers (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    city VARCHAR(80),
    CONSTRAINT uq_publishers_name UNIQUE (name)
);

CREATE TABLE categories (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    CONSTRAINT uq_categories_name UNIQUE (name),
    CONSTRAINT uq_categories_slug UNIQUE (slug),
    CONSTRAINT ck_categories_slug CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$')
);

-- ============ BUKU ============
CREATE TABLE books (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    isbn         VARCHAR(13),
    publish_year INT,
    publisher_id BIGINT NOT NULL,
    CONSTRAINT uq_books_isbn UNIQUE (isbn),
    CONSTRAINT ck_books_isbn CHECK (isbn ~ '^([0-9]{9}[0-9X]|[0-9]{13})$'),
    CONSTRAINT ck_books_publish_year CHECK (publish_year BETWEEN 1000 AND 2100),
    CONSTRAINT fk_books_publisher FOREIGN KEY (publisher_id)
        REFERENCES publishers (id) ON DELETE RESTRICT
);
CREATE INDEX idx_books_publisher_id ON books (publisher_id);
CREATE INDEX idx_books_title        ON books (lower(title));

-- ============ JUNCTION ============
CREATE TABLE book_categories (
    book_id     BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, category_id),
    CONSTRAINT fk_bc_book     FOREIGN KEY (book_id)     REFERENCES books (id)      ON DELETE CASCADE,
    CONSTRAINT fk_bc_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT
);
CREATE INDEX idx_book_categories_category_id ON book_categories (category_id);

CREATE TABLE book_authors (
    book_id   BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_ba_book   FOREIGN KEY (book_id)   REFERENCES books (id)   ON DELETE CASCADE,
    CONSTRAINT fk_ba_author FOREIGN KEY (author_id) REFERENCES authors (id) ON DELETE RESTRICT
);
CREATE INDEX idx_book_authors_author_id ON book_authors (author_id);

-- ============ LOG MEMBACA ============
CREATE TABLE reading_logs (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id     BIGINT      NOT NULL,
    status      VARCHAR(10) NOT NULL,
    started_at  DATE,
    finished_at DATE,
    rating      SMALLINT,
    CONSTRAINT fk_rl_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT ck_rl_status CHECK (status IN ('WISHLIST', 'READING', 'DONE')),
    CONSTRAINT ck_rl_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT ck_rl_dates  CHECK (finished_at IS NULL OR started_at IS NULL OR finished_at >= started_at),
    CONSTRAINT ck_rl_done   CHECK (status <> 'DONE' OR finished_at IS NOT NULL),
    CONSTRAINT ck_rl_rating_done CHECK (rating IS NULL OR status = 'DONE')
);
CREATE INDEX idx_reading_logs_book_id ON reading_logs (book_id);
CREATE INDEX idx_reading_logs_status  ON reading_logs (status);
