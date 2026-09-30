# DEVELOPMENT-SETUP

## Prasyarat
- JDK 21 atau lebih baru
- PostgreSQL 18 berjalan di `localhost:5432`
- (Opsional) Docker untuk Testcontainers, IntelliJ IDEA, Postman

Cek:
```bash
java -version
psql --version
```

## 1. Buat Database
```bash
psql -U postgres -h localhost
```
```sql
CREATE USER bookshelf WITH PASSWORD 'ganti_password_ini';
CREATE DATABASE bookshelf OWNER bookshelf;
\c bookshelf
GRANT ALL ON SCHEMA public TO bookshelf;
\q
```
Verifikasi:
```bash
psql -U bookshelf -h localhost -d bookshelf -c "SELECT current_database();"
```

## 2. Environment Variable
| Variabel | Contoh |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/bookshelf` |
| `SPRING_DATASOURCE_USERNAME` | `bookshelf` |
| `SPRING_DATASOURCE_PASSWORD` | `ganti_password_ini` |

Linux / macOS:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bookshelf
export SPRING_DATASOURCE_USERNAME=bookshelf
export SPRING_DATASOURCE_PASSWORD=ganti_password_ini
```
Windows PowerShell:
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/bookshelf"
$env:SPRING_DATASOURCE_USERNAME="bookshelf"
$env:SPRING_DATASOURCE_PASSWORD="ganti_password_ini"
```
Spring Boot membaca variabel ini otomatis (relaxed binding) — tidak perlu ditulis ulang di `application.yml`.

## 3. Konfigurasi Aplikasi
`src/main/resources/application.yml`:
```yaml
spring:
  application:
    name: bookshelf-api
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration
```
`src/main/resources/application-dev.yml` (seed data hanya di dev):
```yaml
spring:
  flyway:
    locations: classpath:db/migration,classpath:db/seed
```
Migration ada di `src/main/resources/db/migration/V1__init.sql` (isi lengkap: `DATABASE-DESIGN.md`). Seed di `src/main/resources/db/seed/V100__seed_dev.sql`.

## 4. Migration
Flyway berjalan **otomatis saat aplikasi start**. Tidak ada langkah terpisah. Cek hasilnya:
```bash
psql -U bookshelf -h localhost -d bookshelf -c "SELECT version, description, success FROM flyway_schema_history;"
psql -U bookshelf -h localhost -d bookshelf -c "\dt"
```
Reset total database dev:
```sql
DROP SCHEMA public CASCADE; CREATE SCHEMA public;
```

## 5. Menjalankan

### Via Maven Wrapper
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
Windows: `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev`

Server berjalan di `http://localhost:8080`. Cek:
```bash
curl http://localhost:8080/actuator/health
```

### Via IntelliJ IDEA
1. **File → Open** → pilih folder `bookshelf-api` (file `pom.xml`) → *Open as Project*.
2. **Konfigurasi JDK / `JAVA_HOME`:**
   - **File → Project Structure → Project → SDK**: pilih JDK 21+ (atau *Add SDK → Download JDK*). Set *Language level* sesuai.
   - **Settings → Build, Execution, Deployment → Build Tools → Maven → Runner → JRE**: pilih JDK yang sama (jika `./mvnw` dari terminal IDE gagal, ini penyebab umum).
   - `JAVA_HOME` sistem (untuk terminal dan `./mvnw`):
     ```bash
     # Linux / macOS
     export JAVA_HOME=/path/ke/jdk-21
     export PATH="$JAVA_HOME/bin:$PATH"
     ```
     ```powershell
     # Windows (permanen)
     setx JAVA_HOME "C:\Program Files\Java\jdk-21"
     ```
     Restart IntelliJ setelah mengubah variabel sistem.
3. Tunggu Maven import selesai.
4. Buka `BookshelfApiApplication` → **Run → Edit Configurations**:
   - *Active profiles*: `dev`
   - *Environment variables*: `SPRING_DATASOURCE_URL=...;SPRING_DATASOURCE_USERNAME=...;SPRING_DATASOURCE_PASSWORD=...`
5. Klik **Run**. Log akhir yang diharapkan: `Started BookshelfApiApplication`.

## 6. Uji Endpoint

### Postman
Import `bookshelf-api.postman_collection.json`. Variabel `baseUrl` = `http://localhost:8080`. Jalankan berurutan: Penerbit → Penulis → Kategori → Buku → Log.

### curl
Buat penerbit, penulis, kategori:
```bash
curl -s -X POST localhost:8080/api/publishers -H "Content-Type: application/json" \
  -d '{"name":"Bentang Pustaka","city":"Yogyakarta"}'

curl -s -X POST localhost:8080/api/authors -H "Content-Type: application/json" \
  -d '{"name":"Andrea Hirata","nationality":"Indonesia"}'

curl -s -X POST localhost:8080/api/categories -H "Content-Type: application/json" \
  -d '{"name":"Fiksi","slug":"fiksi"}'
```
Buat buku (sesuaikan id dari respons di atas):
```bash
curl -s -X POST localhost:8080/api/books -H "Content-Type: application/json" \
  -d '{"title":"Laskar Pelangi","isbn":"9789793062792","publishYear":2005,"publisherId":1,"authorIds":[1],"categoryIds":[1]}'
```
Daftar, filter, ambil satu:
```bash
curl -s "localhost:8080/api/books"
curl -s "localhost:8080/api/books?category=fiksi&page=0&size=10"
curl -s localhost:8080/api/books/1
```
Ubah sebagian, lalu hapus:
```bash
curl -s -X PATCH localhost:8080/api/books/1 -H "Content-Type: application/json" -d '{"publishYear":2006}'
curl -i -X DELETE localhost:8080/api/books/1
```
Uji error 409 (ISBN duplikat) — kirim `POST /api/books` dua kali dengan ISBN sama:
```bash
curl -i -X POST localhost:8080/api/books -H "Content-Type: application/json" \
  -d '{"title":"Duplikat","isbn":"9789793062792","publishYear":2005,"publisherId":1,"authorIds":[1],"categoryIds":[1]}'
```

## Troubleshooting
| Gejala | Penyebab umum |
|---|---|
| `Connection refused` | PostgreSQL belum jalan / port salah |
| `password authentication failed` | Env var salah; cek user di `psql` |
| `Schema-validation: missing table` | Migration belum jalan; cek `flyway_schema_history` |
| `Migration checksum mismatch` | Migration lama diedit. Kembalikan, atau reset DB dev |
| `release version 21 not supported` | Maven/IDE memakai JDK lama; cek `JAVA_HOME` dan Runner JRE |
| Port 8080 dipakai | `SERVER_PORT=8081` |
