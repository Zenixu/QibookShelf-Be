# 🚀 Quick Start Guide - QibookShelf API

## Prerequisites

- ✅ Java 25 installed
- ✅ PostgreSQL 14+ running
- ✅ Maven 3.9+ (atau gunakan `./mvnw`)

---

## 📦 Step-by-Step Setup (5 Menit)

### 1. Clone & Navigate
```bash
cd QibookShelf-Be
```

### 2. Setup Database
```bash
# Create database
createdb bookshelf

# Or using psql
psql -U postgres -c "CREATE DATABASE bookshelf;"
```

### 3. Configure Environment
```bash
# Copy environment template
cp .env.example .env

# Edit .env file dengan nilai yang sesuai
# Minimal yang harus diisi:
# - DB_USER
# - DB_PASSWORD  
# - JWT_SECRET (minimal 32 karakter)
```

**Generate JWT Secret:**
```bash
# Linux/Mac:
openssl rand -base64 32

# Windows (PowerShell):
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

### 4. Set Environment Variables
```bash
# Linux/Mac:
export DB_USER=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=your-generated-secret-key-here

# Windows (cmd):
set DB_USER=postgres
set DB_PASSWORD=your_password
set JWT_SECRET=your-generated-secret-key-here

# Windows (PowerShell):
$env:DB_USER="postgres"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your-generated-secret-key-here"
```

### 5. Run Application
```bash
# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# Or using installed Maven
mvn spring-boot:run
```

✅ API akan berjalan di `http://localhost:8080`

---

## 🧪 Test API (Postman/cURL)

### 1️⃣ Register User Baru
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "username": "johndoe",
    "password": "password123",
    "fullName": "John Doe"
  }'
```

**Expected Response:**
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
    "createdAt": "2026-09-30T07:00:00"
  }
}
```

### 2️⃣ Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

### 3️⃣ Get Books (with Token)
```bash
# Ganti <TOKEN> dengan accessToken dari response register/login
curl http://localhost:8080/api/books \
  -H "Authorization: Bearer <TOKEN>"
```

### 4️⃣ Create Book
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code",
    "isbn": "9780132350884",
    "publishYear": 2008,
    "publisherId": 1,
    "authorIds": [1],
    "categoryIds": [1]
  }'
```

---

## 🎯 Quick Commands

```bash
# Build project
./mvnw clean install

# Run tests
./mvnw test

# Package as JAR
./mvnw package

# Run without Maven wrapper
java -jar target/bookshelf-api-0.0.1-SNAPSHOT.jar

# Check health
curl http://localhost:8080/actuator/health
```

---

## 🔧 Troubleshooting

### Database Connection Failed
```bash
# Check PostgreSQL is running
pg_isready

# Check database exists
psql -U postgres -l | grep bookshelf

# Recreate database if needed
dropdb bookshelf
createdb bookshelf
```

### Port 8080 Already in Use
```bash
# Change port in application.yml
server:
  port: 8081

# Or set environment variable
export SERVER_PORT=8081
```

### JWT Secret Too Short
```
Error: JWT_SECRET must be at least 256 bits (32 characters)
```
Generate a proper secret key (see step 3 above).

### Migration Failed
```bash
# Check Flyway history
psql -d bookshelf -c "SELECT * FROM flyway_schema_history;"

# Repair if needed (advanced)
./mvnw flyway:repair
```

---

## 📚 Next Steps

✅ **You're ready!** Baca dokumentasi lengkap:
- `README.md` - Project overview
- `SECURITY.md` - Authentication guide
- `CHANGELOG.md` - Version history

---

## 🎓 Development Tips

1. **Use Postman Collection** untuk testing API
2. **Enable SQL logging** untuk debugging query
   ```yaml
   # application-dev.yml
   spring:
     jpa:
       show-sql: true
   ```
3. **Hot reload** otomatis dengan Spring DevTools (sudah included)
4. **Database GUI** - gunakan DBeaver, pgAdmin, atau TablePlus

---

**Happy coding!** 🚀
