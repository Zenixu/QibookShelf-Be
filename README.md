# QibookShelf API

Backend REST API untuk pencatat koleksi buku pribadi dan riwayat membacanya.

## 🎯 Features

- ✅ **CRUD Buku** - Kelola koleksi buku pribadi
- ✅ **Multi-author & Multi-category** - Buku bisa punya banyak penulis dan kategori
- ✅ **Reading Logs** - Catat riwayat membaca dengan status (WISHLIST, READING, DONE)
- ✅ **Rating System** - Beri rating 1-5 untuk buku yang sudah selesai dibaca
- ✅ **Advanced Filtering** - Filter buku berdasarkan kategori, penulis, judul, tahun terbit
- ✅ **JWT Authentication** - Keamanan dengan JSON Web Token
- ✅ **Multi-tenancy** - Setiap user punya koleksi buku sendiri

## 🛠️ Tech Stack

- **Java 25** (LTS)
- **Spring Boot 4.1.1** (bleeding edge 2026)
- **Spring Security** + JWT
- **PostgreSQL** + Flyway migration
- **Spring Data JPA** + Hibernate
- **Maven**

## 🚀 Quick Start

### Prerequisites

- **JDK 25+** (`pom.xml` memakai `--release 25`). Jika JDK 25 belum terpasang,
  JDK 26 tetap bisa dipakai:

  ```bash
  export JAVA_HOME=/usr/lib/jvm/java-26-openjdk   # Linux, mesin ini
  ./mvnw -version                                  # pastikan Java 26/25
  ```

  JDK 21 **tidak** bisa membangun proyek ini (`release version 25 not supported`).
- PostgreSQL 14+ *(opsional untuk pengembangan — test memakai Testcontainers)*
- Maven 3.9+ (atau lewat wrapper `./mvnw`)

### 1. Setup Database

```bash
createdb bookshelf
```

### 2. Environment Variables

```bash
export DB_USER=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=your-256-bit-secret-key-change-this-in-production-min-32-chars
```

### 3. Run Application

```bash
mvn spring-boot:run
```

API akan berjalan di `http://localhost:8080`

## 📚 Documentation

Dokumentasi lengkap tersedia di folder `docs/`:
- **[docs/README.md](docs/README.md)** - Documentation index
- **[docs/QUICKSTART.md](docs/QUICKSTART.md)** - 5-minute setup guide
- **[docs/SECURITY.md](docs/SECURITY.md)** - Authentication & authorization
- **[docs/CHANGELOG.md](docs/CHANGELOG.md)** - Version history
- **[docs/UPGRADE-SUMMARY.md](docs/UPGRADE-SUMMARY.md)** - Technical details

### Quick Test

```bash
# 1. Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "username": "johndoe",
    "password": "password123",
    "fullName": "John Doe"
  }'

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'

# 3. Get books (gunakan token dari response login)
curl http://localhost:8080/api/books \
  -H "Authorization: Bearer <your-access-token>"
```

## 📁 Project Structure

```
src/main/java/dev/bookshelf/
├── auth/                   # Authentication endpoints
├── security/               # JWT, User entity, SecurityConfig
├── book/                   # Book domain
├── author/                 # Author domain
├── publisher/              # Publisher domain
├── category/               # Category domain
├── readinglog/             # Reading log domain
└── common/                 # Shared utilities & exceptions

src/main/resources/
├── application.yml         # Configuration
└── db/migration/          # Flyway migrations
    ├── V1__init.sql       # Initial schema
    └── V2__add_users_and_security.sql  # Security tables
```

## 🔐 Security

- **Password hashing** dengan BCrypt
- **JWT tokens** untuk stateless authentication
- **Multi-tenancy** - User isolation di database level
- **SQL injection protection** via JPA parameterized queries
- **CORS** configured untuk frontend development

## 🧪 Testing

```bash
# Build penuh + seluruh test (butuh Docker untuk Testcontainers)
export JAVA_HOME=/usr/lib/jvm/java-26-openjdk
./mvnw clean verify
```

34 tes: 29 end-to-end (MockMvc → service → repository → PostgreSQL di Testcontainers)
dan 4 repository. Test E2E mendaftar & login sungguhan lalu mengirim
`Authorization: Bearer <token>` di setiap request, sehingga JWT dan
multi-tenancy ikut teruji.

```bash
# Run all tests
./mvnw test
```

## 🚢 Deployment

### Docker (TODO)

```bash
docker-compose up -d
```

### Production Checklist

- [ ] Ganti `JWT_SECRET` dengan secret key yang kuat
- [ ] Setup HTTPS/TLS
- [ ] Configure database connection pooling
- [ ] Enable production logging
- [ ] Setup monitoring & alerting
- [ ] Configure backup strategy

## 📊 Database Schema

![ER Diagram](docs/er-diagram.png) _(TODO)_

**8 Tables:**
- `users` - User accounts
- `authors` - Penulis buku
- `publishers` - Penerbit
- `categories` - Kategori buku
- `books` - Data buku
- `reading_logs` - Riwayat membaca
- `book_authors` + `book_categories` - Junction tables

## 🎓 Learning Project

Project ini dibuat untuk:
- ✅ Belajar Spring Boot 4 + Java 25
- ✅ Implementasi JWT authentication
- ✅ Multi-tenancy pattern
- ✅ Clean architecture
- ✅ Database migration dengan Flyway
- ✅ REST API best practices

## 📝 License

MIT License - Bebas digunakan untuk belajar dan portfolio.

## 👨‍💻 Author

Project PKL - QibookShelf Backend API

---

**Version:** 2.0.0 (with Security)  
**Updated:** September 30, 2026
