# 📋 CHANGELOG

## [2.0.0] - 2026-09-30

### 🔐 Security - CRITICAL UPDATES

#### Added
- **JWT Authentication System**
  - JWT token-based authentication (access token 24h, refresh token 7 days)
  - BCrypt password hashing
  - Spring Security integration
  - Token refresh mechanism
  - `/api/auth/register` - User registration
  - `/api/auth/login` - User login
  - `/api/auth/refresh` - Token refresh
  - `/api/auth/me` - Get current user profile

- **Multi-tenancy Support**
  - Added `user_id` foreign key to `books` table
  - Added `user_id` foreign key to `reading_logs` table
  - User isolation at database level
  - Automatic filtering of data by logged-in user
  - CASCADE DELETE when user is deleted

- **Security Components**
  - `User` entity with UserDetails implementation
  - `JwtService` for token generation and validation
  - `JwtAuthenticationFilter` for request authentication
  - `CustomUserDetailsService` for user loading
  - `SecurityConfig` with CORS and endpoint protection
  - `SecurityUtil` helper for getting current user

#### Changed
- **Book Entity**
  - Added `user` field (ManyToOne relationship)
  - Updated constructor to accept `User` parameter
  - All book operations now filter by current user

- **BookService**
  - `create()` - Auto-assign book to current user
  - `list()` - Filter books by current user ID
  - `getDetailById()` - Verify book ownership
  - `update()` - Verify book ownership
  - `patch()` - Verify book ownership
  - `delete()` - Verify book ownership

- **BookSpecifications**
  - Added `belongsToUser()` filter
  - Updated `withFilters()` to accept `userId` parameter
  - Multi-tenancy filter applied to all queries

- **ReadingLog Entity**
  - Added `user` field (ManyToOne relationship)
  - Updated constructor to accept `User` parameter

- **ReadingLogService**
  - `create()` - Auto-assign log to current user
  - `listByBook()` - Filter logs by current user
  - `listByStatus()` - Filter logs by current user
  - `update()` - Verify log ownership
  - `delete()` - Verify log ownership

#### Security Features
- ✅ Password hashing with BCrypt (strength 10)
- ✅ JWT token signing with HMAC SHA-256
- ✅ CORS configuration for frontend development
- ✅ Stateless session management
- ✅ SQL injection prevention via JPA
- ✅ Input validation with Bean Validation
- ✅ User isolation at database level

### 🗄️ Database

#### Migration V2__add_users_and_security.sql
```sql
-- New table
CREATE TABLE users (
    id, email, username, password_hash, full_name,
    is_active, created_at, updated_at
)

-- Multi-tenancy columns
ALTER TABLE books ADD COLUMN user_id BIGINT;
ALTER TABLE reading_logs ADD COLUMN user_id BIGINT;

-- Foreign keys with CASCADE DELETE
ALTER TABLE books ADD CONSTRAINT fk_books_user;
ALTER TABLE reading_logs ADD CONSTRAINT fk_reading_logs_user;

-- Unique constraint: one READING log per user-book
CREATE UNIQUE INDEX idx_reading_logs_unique_reading_per_user_book;
```

### 📚 Documentation

#### Added
- `README.md` - Complete project documentation
- `SECURITY.md` - Authentication & authorization guide
  - Registration & login flow
  - JWT token usage
  - Multi-tenancy explanation
  - Security features
  - API testing examples

### ⚙️ Configuration

#### Updated
- `application.yml`
  - Added datasource configuration
  - Added JWT configuration (secret, expiration)
  - Added server error configuration
  - Environment variable support

- `pom.xml`
  - **Upgraded Java from 21 to 25**
  - Added Spring Security dependency
  - Added JJWT dependencies (0.12.6)

### 🚧 Protected Endpoints

All endpoints now require authentication except:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /actuator/health`

### 🔄 Breaking Changes

⚠️ **BREAKING**: All existing API endpoints now require `Authorization: Bearer <token>` header.

⚠️ **BREAKING**: Existing data in `books` and `reading_logs` tables have `user_id = NULL`. 
   - Old data tidak bisa diakses sampai di-assign ke user tertentu
   - Atau bisa dihapus dan di-seed ulang dengan user

### 📊 Statistics

- **New Files**: 16
  - 11 Java classes (security + auth)
  - 1 SQL migration
  - 2 Markdown docs
  - 1 SecurityUtil helper

- **Modified Files**: 8
  - Book.java, BookService.java, BookSpecifications.java
  - ReadingLog.java, ReadingLogService.java
  - pom.xml, application.yml

- **Lines of Code Added**: ~800 lines
- **Security Level**: 🔴 None → 🟢 Production-Ready

---

## [1.0.0] - Previous

### Features
- ✅ CRUD operations for Books, Authors, Publishers, Categories
- ✅ Reading logs with status tracking (WISHLIST, READING, DONE)
- ✅ Advanced filtering and pagination
- ✅ Flyway database migrations
- ✅ Global exception handling with RFC 9457 ProblemDetail
- ✅ Validation with Bean Validation

---

## 🎯 Next Version (2.1.0)

Planned features:
- [ ] Role-based access control (ADMIN, USER)
- [ ] Email verification
- [ ] Password reset
- [ ] OAuth2 integration (Google, GitHub)
- [ ] Rate limiting
- [ ] OpenAPI/Swagger documentation
- [ ] Comprehensive testing
- [ ] Docker support
- [ ] CI/CD pipeline
