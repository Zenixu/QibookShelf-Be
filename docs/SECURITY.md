# 🔐 QibookShelf API - Security Documentation

## Authentication & Authorization

QibookShelf sekarang menggunakan **JWT (JSON Web Token)** untuk autentikasi dan **Multi-tenancy** untuk isolasi data antar user.

---

## 🚀 Endpoints

### **Public Endpoints (Tidak Butuh Token)**

```http
POST /api/auth/register     # Registrasi user baru
POST /api/auth/login        # Login dan dapatkan token
POST /api/auth/refresh      # Refresh access token
GET  /actuator/health       # Health check
```

### **Protected Endpoints (Butuh Token)**

```http
GET  /api/auth/me           # Profil user yang sedang login
GET  /api/books             # List buku milik user
POST /api/books             # Tambah buku (otomatis link ke user)
# ... semua endpoint lainnya
```

---

## 📝 Authentication Flow

### 1. **Register User Baru**

```bash
POST /api/auth/register
Content-Type: application/json

{
  "email": "john@example.com",
  "username": "johndoe",
  "password": "password123",
  "fullName": "John Doe"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "john@example.com",
    "username": "johndoe",
    "fullName": "John Doe",
    "isActive": true,
    "createdAt": "2026-09-30T06:58:40"
  }
}
```

### 2. **Login**

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}
```

**Response:** Sama seperti register

### 3. **Menggunakan Token**

Setiap request ke protected endpoint harus menyertakan token di header:

```bash
GET /api/books
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4. **Refresh Token**

Ketika access token expired (24 jam), gunakan refresh token untuk mendapatkan token baru:

```bash
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 🔒 Multi-Tenancy

Setiap user hanya bisa melihat dan memodifikasi **data miliknya sendiri**:

- ✅ User A tidak bisa lihat buku User B
- ✅ User A tidak bisa edit/hapus buku User B
- ✅ User A tidak bisa lihat reading log User B
- ✅ Semua data otomatis di-filter berdasarkan `user_id`

### Contoh Scenario

**User A (johndoe):**
```bash
POST /api/books
Authorization: Bearer <token_user_a>

{
  "title": "Clean Code",
  "isbn": "9780132350884",
  "publishYear": 2008,
  "publisherId": 1,
  "authorIds": [1],
  "categoryIds": [1]
}
```
→ Buku tersimpan dengan `user_id = 1` (johndoe)

**User B (janedoe):**
```bash
GET /api/books
Authorization: Bearer <token_user_b>
```
→ Tidak akan melihat buku "Clean Code" milik User A

---

## 🛡️ Security Features

### 1. **Password Hashing**
- Password di-hash dengan **BCrypt** (strength 10)
- Password asli tidak pernah disimpan di database

### 2. **JWT Token**
- **Access Token:** 24 jam
- **Refresh Token:** 7 hari
- Signed dengan HMAC SHA-256
- Secret key di environment variable `JWT_SECRET`

### 3. **CORS Configuration**
- Default: `localhost:3000` dan `localhost:5173`
- Bisa dikonfigurasi di `SecurityConfig.java`

### 4. **SQL Injection Prevention**
- Semua query menggunakan JPA/Hibernate (parameterized)
- Validasi input dengan Bean Validation

### 5. **Authorization**
- Setiap endpoint protected
- User hanya bisa akses data miliknya sendiri
- Foreign key cascade untuk data cleanup

---

## ⚙️ Configuration

### Environment Variables

```env
# Database
DB_USER=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-256-bit-secret-key-change-this-in-production-min-32-chars

# Server
SERVER_PORT=8080
```

### application.yml

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000      # 24 hours
  refresh-expiration: 604800000  # 7 days
```

---

## 🧪 Testing

### Postman Collection

```bash
# 1. Register
POST localhost:8080/api/auth/register
Body: { "email": "test@example.com", "username": "testuser", "password": "test1234", "fullName": "Test User" }

# 2. Login
POST localhost:8080/api/auth/login
Body: { "username": "testuser", "password": "test1234" }

# 3. Copy accessToken dari response

# 4. Test Protected Endpoint
GET localhost:8080/api/books
Header: Authorization: Bearer <accessToken>
```

---

## 🔑 Database Schema

### Table: `users`

```sql
CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150),
    is_active     BOOLEAN NOT NULL DEFAULT true,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Foreign Keys

```sql
-- books.user_id → users.id (CASCADE DELETE)
-- reading_logs.user_id → users.id (CASCADE DELETE)
```

Ketika user dihapus, semua buku dan reading log miliknya juga ikut terhapus.

---

## 🚨 Error Responses

### 401 Unauthorized
```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Full authentication is required to access this resource",
  "instance": "handleNotAuthenticated"
}
```

### 403 Forbidden
```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "Access denied",
  "instance": "handleAccessDenied"
}
```

### 409 Conflict (Duplicate User)
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Username 'johndoe' sudah terdaftar",
  "instance": "register"
}
```

---

## 📊 Migration Status

✅ **V1__init.sql** - Skema awal (authors, publishers, categories, books, reading_logs)  
✅ **V2__add_users_and_security.sql** - Tabel users + kolom user_id di books & reading_logs

---

## 🎯 Next Steps

1. ✅ Setup JWT authentication
2. ✅ Multi-tenancy implementation
3. ⏳ Role-based access control (RBAC)
4. ⏳ Email verification
5. ⏳ Password reset
6. ⏳ OAuth2 integration (Google, GitHub)
7. ⏳ Rate limiting
8. ⏳ API documentation (Swagger/OpenAPI)

---

Dokumentasi ini dibuat untuk **QibookShelf v2.0** dengan Java 25 dan Spring Boot 4.1.1
