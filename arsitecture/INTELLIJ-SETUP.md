# INTELLIJ-SETUP.md — Menjalankan `bookshelf-api` di IntelliJ IDEA

Panduan langkah demi langkah menjalankan dan men-debug aplikasi lewat IntelliJ IDEA (IDE JetBrains). Untuk setup umum via terminal, lihat `DEVELOPMENT-SETUP.md`.

> **Catatan versi:** proyek memakai **Spring Boot 4.1.1** dan target **Java 25** (`pom.xml` → `<java.version>25</java.version>`). Praktik di lapangan: **JDK 26** (`/usr/lib/jvm/java-26-openjdk`) aman dipakai; **JDK 21 GAGAL** dengan error `release version 25 not supported`.

---

## Ringkasan Cepat (TL;DR)

| Langkah | Aksi |
|---|---|
| 1 | `File → Open` folder proyek (yang berisi `pom.xml`) |
| 2 | Set **Project SDK = JDK 26** |
| 3 | Set **Maven Runner JRE = JDK 26** |
| 4 | Aktifkan **Annotation Processing** |
| 5 | Buat Run Configuration: main class + **profile `dev`** + env vars |
| 6 | Jalankan **PostgreSQL** |
| 7 | Klik ▶ **Run** (atau 🐞 **Debug**) |

---

## 0. Prasyarat

- IntelliJ IDEA terpasang (Community atau Ultimate). Spring Boot plugin **tidak wajib** — cukup jalankan sebagai aplikasi Java biasa.
- JDK 26 tersedia di sistem. Verifikasi:
  ```bash
  ls /usr/lib/jvm/
  java -version
  ```
- PostgreSQL 18 berjalan & database `bookshelf` sudah ada (lihat `DEVELOPMENT-SETUP.md`):
  ```bash
  sudo systemctl start postgresql
  ```

---

## 1. Buka Project

1. **File → Open**.
2. Pilih folder proyek (`Qibookshelf-BE`, folder yang berisi `pom.xml`) → **OK**.
3. Jika muncul dialog **Trust Project** → klik **Trust Project**.
4. IntelliJ mengimpor sebagai proyek **Maven** secara otomatis. Tunggu progress *"Resolving Maven dependencies"* selesai.

> Proyek ini tidak menyertakan folder `.idea/` (ada di `.gitignore`), jadi konfigurasi IDE dibuat lokal oleh masing-masing developer dan **tidak ikut ter-commit**.

---

## 2. Set JDK (WAJIB — JDK 26)

**File → Project Structure** (`Ctrl+Alt+Shift+S`):

- Tab **Project**
  - **SDK** → **Add SDK → JDK** → pilih `/usr/lib/jvm/java-26-openjdk` (atau **Download JDK…** bila belum ada).
  - **Language level** → **SDK default** (kode mengikuti `pom.xml`, tidak perlu diubah manual).
- Tab **SDKs**
  - Pastikan `java-26-openjdk` terdaftar.
- Tab **Modules → Sources**
  - **Language level** = **25 / SDK default**.

Fungsi JDK 26 di sini adalah meng-*build* sesuai `--release 25`. Menggunakan JDK 21 akan menggagalkan kompilasi.

---

## 3. Set Maven Runner JRE (WAJIB)

Jika `./mvnw` dijalankan dari dalam IntelliJ memakai JDK yang salah, build gagal meski Project SDK sudah benar.

**Settings** (`Ctrl+Alt+S`) → **Build, Execution, Deployment → Build Tools → Maven → Runner**:
- **JRE** → pilih `java-26-openjdk` (atau *Use Project JDK*).

---

## 4. Aktifkan Annotation Processing

Diperlukan agar pemrosesan anotasi saat kompilasi berjalan normal.

**Settings → Build, Execution, Deployment → Compiler → Annotation Processors**:
- ✅ centang **Enable annotation processing**.

---

## 5. Buat Run Configuration

Karena Spring Boot plugin bisa saja tidak ada, gunakan konfigurasi **Application** biasa:

1. **Run → Edit Configurations…** → **+ → Application** (atau *Spring Boot* bila plugin tersedia).
2. Isi:

   | Field | Nilai |
   |---|---|
   | **Name** | `BookshelfApi (dev)` |
   | **Main class** | `dev.bookshelf.BookshelfApiApplication` |
   | **Use classpath of module** | `Qibookshelf-BE` |
   | **JRE** | `java-26-openjdk` (atau *Project SDK*) |
   | **Active profiles** | `dev` |
   | **Environment variables** | lihat tabel di bawah |

3. **Environment variables** (klik ikon 📋). Aplikasi membaca `${DB_USER}`, `${DB_PASSWORD}`, `${JWT_SECRET}` dari `application.yml`:
   ```text
   DB_USER=postgres;DB_PASSWORD=postgres;JWT_SECRET=<secret-min-32-karakter>
   ```
   > Salin nilai dari file `.env` lokal, atau buat secret baru:
   > ```bash
   > openssl rand -base64 48
   > ```

4. **Apply → OK**.

### Alternatif: Plugin EnvFile

Jika menyukai satu sumber konfigurasi:
1. Install plugin **EnvFile** (Settings → Plugins → Marketplace).
2. Di Run Configuration, centang **Enable EnvFile** → tambahkan file `.env`.

File `.env` sudah ada di `.gitignore`, jadi rahasia tidak ikut ter-commit.

> **⚠️ Penting — `.env` TIDAK dibaca otomatis.** Spring Boot hanya membaca **environment variable** (dan `application.yml`), **bukan** file `.env`. Karena itu, di IntelliJ kamu **wajib** memakai salah satu cara di atas (isi env var manual di Run Configuration, **atau** pasang plugin EnvFile). Tanpa keduanya, aplikasi memakai nilai *fallback* di `application.yml` (`postgres`/`postgres`) dan **`JWT_SECRET` placeholder** — yang bisa membuat login gagal atau JWT tidak aman.
>
> Di terminal, ini dilakukan lewat `set -a; . ./.env; set +a` (lihat `DEVELOPMENT-SETUP.md`) — file `.env` di-*source* ke environment **dulu**, baru Spring bisa membacanya. IntelliJ tidak melakukan `source` otomatis, jadi itulah fungsi plugin EnvFile / kolom Environment variables.

---

## 6. Jalankan PostgreSQL

```bash
sudo systemctl start postgresql
```

Database `bookshelf` harus sudah ter-migrasi (Flyway V1 + V2 + V100). Flyway berjalan **otomatis** saat aplikasi start.

---

## 7. Run / Debug

1. Pilih konfigurasi **`BookshelfApi (dev)`** di toolbar atas.
2. Klik ▶ **Run** atau 🐞 **Debug** (mendukung breakpoint).
3. Tunggu hingga log menampilkan:
   ```text
   Started BookshelfApiApplication in X.XXX seconds
   ```
4. Verifikasi dari terminal:
   ```bash
   curl http://localhost:8080/actuator/health
   # {"groups":["liveness","readiness"],"status":"UP"}
   ```

---

## 8. Uji Endpoint dari IntelliJ (file `.http`)

IntelliJ punya HTTP Client bawaan — tak perlu Postman. Buat file `requests.http` di root proyek:

```http
### Health (tanpa auth)
GET http://localhost:8080/actuator/health

### Login — dapatkan token
# Kredensial seed (profil dev): demo/demo12345 atau sinta/sinta12345
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{"username":"demo","password": "***"}

> {% client.global.set("token", response.body.accessToken); %}

### Daftar buku (butuh token)
GET http://localhost:8080/api/books
Authorization: Bearer {{token}}

### Daftar author (paginasi)
GET http://localhost:8080/api/authors?page=0&size=10
Authorization: Bearer {{token}}

### Daftar reading-log global
GET http://localhost:8080/api/reading-logs
Authorization: Bearer {{token}}
```

Klik ikon ▶ di sebelah kiri setiap request untuk menjalankannya.

**Kredensial seed** (profil `dev`):

| Username | Password | Email |
|---|---|---|
| `demo` | `demo12345` | `demo@bookshelf.dev` |
| `sinta` | `sinta12345` | `sinta@bookshelf.dev` |

---

## 9. Menjalankan Test di IntelliJ

- Klik kanan pada folder `src/test/java` (atau file test tertentu) → **Run 'Tests in …'**.
- Test E2E memakai **Testcontainers** → **Docker harus berjalan**.
- Hasil yang diharapkan: **34 test hijau** (BookRepositoryTest 4, BookshelfEndToEndTest 30).

---

## Troubleshooting

| Gejala | Penyebab umum | Solusi |
|---|---|---|
| `release version 25 not supported` | Build memakai JDK 21 | Set Project SDK **dan** Maven Runner JRE ke JDK 26 |
| Class tidak ditemukan / compile error aneh | Annotation processing mati | Aktifkan Annotation Processing (langkah 4) |
| `Connection refused` saat start | PostgreSQL belum jalan | `sudo systemctl start postgresql` |
| `password authentication failed` | `DB_PASSWORD` salah | Perbaiki env var di Run Configuration |
| Start sukses tapi login `demo` gagal | Profil `dev` belum aktif → seed tidak dimuat | Set **Active profiles = `dev`** |
| `Migration checksum mismatch` | DB dev kedaluwarsa | Reset DB (lihat di bawah) |
| `Port 8080 was already in use` | Instance lain masih jalan | Matikan instance itu, atau set `SERVER_PORT=8081` |
| `Schema-validation: missing table` | Migrasi belum jalan | Cek `flyway_schema_history`; muat ulang DB |
| JWT error saat start | `JWT_SECRET` kosong / < 32 karakter | Isi `JWT_SECRET` yang valid |
| First Run sangat lama | Maven masih mengunduh dependency | Tunggu hingga progress selesai |

### Reset DB dev

Aman dilakukan karena DB dev hanya berisi data seed:

```bash
psql -h localhost -U postgres -d postgres -c "DROP DATABASE bookshelf;"
psql -h localhost -U postgres -d postgres -c "CREATE DATABASE bookshelf;"
```
Jalankan ulang aplikasi → Flyway memigrasi dari nol (V1 + V2 + V100).

---

## Lampiran: Daftar Variabel Lingkungan

| Variabel | Wajib | Default (`application.yml`) | Keterangan |
|---|---|---|---|
| `DB_USER` | tidak | `postgres` | User PostgreSQL |
| `DB_PASSWORD` | tidak | `postgres` | Password PostgreSQL |
| `JWT_SECRET` | **ya** (≥ 32 karakter) | nilai placeholder | Kunci penandatanganan JWT |
| `SERVER_PORT` | tidak | `8080` | Port HTTP server |
| `DB_URL` | tidak | `jdbc:postgresql://localhost:5432/bookshelf` | Override URL JDBC |

---

Lihat juga: `DEVELOPMENT-SETUP.md` (setup terminal/Maven), `TECH-STACK.md` (dependency), `API-CONTRACT.md` (spesifikasi endpoint).
