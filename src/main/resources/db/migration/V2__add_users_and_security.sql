-- ============ USER & AUTHENTICATION ============
CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    username      VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150),
    is_active     BOOLEAN NOT NULL DEFAULT true,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT ck_users_email CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_username ON users (username);

-- Trigger untuk update updated_at otomatis
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============ MULTI-TENANCY: Add user_id ke books ============
ALTER TABLE books ADD COLUMN user_id BIGINT;
ALTER TABLE books ADD CONSTRAINT fk_books_user 
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
CREATE INDEX idx_books_user_id ON books (user_id);

-- Karena data existing tidak punya user, kita set nullable dulu
-- Nanti di aplikasi, semua book baru WAJIB punya user_id

-- ============ MULTI-TENANCY: Add user_id ke reading_logs ============
ALTER TABLE reading_logs ADD COLUMN user_id BIGINT;
ALTER TABLE reading_logs ADD CONSTRAINT fk_reading_logs_user 
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
CREATE INDEX idx_reading_logs_user_id ON reading_logs (user_id);

-- ============ COMPOSITE UNIQUE: Satu buku hanya boleh punya 1 active reading log per user ============
-- User tidak boleh punya 2 reading log dengan status READING untuk buku yang sama
CREATE UNIQUE INDEX idx_reading_logs_unique_reading_per_user_book 
    ON reading_logs (user_id, book_id, status) 
    WHERE status = 'READING';
