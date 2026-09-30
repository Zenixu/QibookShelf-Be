# TASKS

Kerjakan berurutan. Centang `[x]` saat selesai. Satu fase = satu commit/PR kecil.

> **Status per 30 Sep 2026** — `./mvnw clean verify` **BUILD SUCCESS, 34 test hijau**
> (29 E2E + 4 repository, jalankan dengan `JAVA_HOME=/usr/lib/jvm/java-26-openjdk`).
> Centang di bawah hanya untuk item yang benar-benar terverifikasi.

## Fase 0 — Persiapan
- [x] Pasang JDK 21+, verifikasi `java -version`
  (mesin ini: default 21, build memakai **JDK 26** di `/usr/lib/jvm/java-26-openjdk`
  karena `pom.xml` target `--release 25`)
- [ ] Pasang PostgreSQL 18, buat user + database `bookshelf` (`DEVELOPMENT-SETUP.md` §1)
  — **belum**: `localhost:5432` tidak menjawab. Semua test memakai Testcontainers
  (`postgres:18-alpine`), jadi pengembangan bisa jalan tanpa PG lokal.
- [ ] Set environment variable `SPRING_DATASOURCE_*`
  — aplikasi memakai `DB_USER` / `DB_PASSWORD` / `JWT_SECRET` dengan nilai default
  di `application.yml`; belum ada `.env` aktif (lihat `.env.example`)
- [x] Simpan seluruh dokumen `.md` di folder `docs/` repo

## Fase 1 — Init Proyek
- [x] Generate proyek: Maven, Java 25, Spring Boot 4.1.1, group `dev.bookshelf`, artifact `bookshelf-api`
- [x] Tambah dependency sesuai `TECH-STACK.md` (webmvc, data-jpa, validation, flyway, flyway-database-postgresql, postgresql, actuator) + security & testcontainers
- [x] Inisialisasi git, `.gitignore` (target/, .idea/, .env)
- [x] Buat `application.yml` dan `application-dev.yml`
- [x] Aplikasi start tanpa error (terbukti lewat context load di `@SpringBootTest`)

## Fase 2 — Database & Migration
- [x] Buat `db/migration/V1__init.sql` dari `DATABASE-DESIGN.md` §2
- [x] Buat `db/seed/V100__seed_dev.sql` dari §5 (aktif hanya di profil `dev`)
- [ ] Jalankan aplikasi profil `dev`; cek `flyway_schema_history` dan `\dt`
  — **belum** (butuh PG lokal); jalur migrasi terverifikasi lewat Testcontainers
- [x] Uji constraint (ISBN duplikat → 409, `rating` di luar DONE → 409, `DONE` tanpa `finishedAt` → 409)
- [x] `ddl-auto=validate` lolos setelah entity dibuat

## Fase 3 — Entity & Repository
- [x] Entity `Author`, `Publisher`, `Category`
- [x] Entity `Book` (`@ManyToOne Publisher`, `@ManyToMany Set<Author>`, `@ManyToMany Set<Category>`, semua `LAZY`)
- [x] Entity `ReadingLog` + `enum ReadingStatus`
- [x] Repository per entity
- [x] Query filter buku: kategori (slug), penulis, judul, tahun — tanpa N+1 (`@EntityGraph`)
- [x] `@DataJpaTest` (Testcontainers): `BookRepositoryTest` — 4 tes hijau

## Fase 4 — Fondasi API
- [x] `PageResponse<T>` (`content, page, size, totalElements, totalPages`)
- [x] Exception: `NotFoundException`, `ConflictException`
- [x] `@RestControllerAdvice` → `ProblemDetail`: 400 (validasi + JSON rusak + tipe parameter), 404, 409
- [ ] `ProblemDetail` untuk **500** — belum ada `@ExceptionHandler(Exception.class)`;
      kalau ditambah harus mendelegasikan error bawaan Spring MVC (405/415/404) ke resolver berikutnya
- [x] Tangani `DataIntegrityViolationException` → 409 (ISBN/nama/slug duplikat, FK RESTRICT)

## Fase 5 — CRUD Master Data
- [x] Authors: DTO, service, controller, endpoint `API-CONTRACT.md` (POST/GET/PUT/**PATCH**/DELETE)
- [x] Publishers: idem
- [x] Categories: idem (validasi pola `slug`)
- [x] Hapus data yang masih dipakai → 409 dengan pesan jelas
- [ ] Test `@WebMvcTest` per controller (sukses + 400 + 404 + 409)
  — belum ada `@WebMvcTest`; kasus 400/404/409 sudah tercakup `BookshelfEndToEndTest`

## Fase 6 — CRUD Buku
- [x] DTO `BookRequest`, `BookPatchRequest`, respons detail/summary (publisher, authors, categories ter-embed)
- [x] `POST /api/books` (validasi id publisher/author/category → 404; ISBN duplikat → 409)
- [x] `GET /api/books/{id}` dan `GET /api/books` + paginasi + sort
- [x] Filter `?category=`, `?author=`, `?q=`, `?year=`
- [x] `PATCH /api/books/{id}` (set penulis/kategori = ganti seluruhnya)
- [x] `DELETE /api/books/{id}` (cascade ke junction dan log terverifikasi di test)
- [x] Verifikasi jumlah query pada list buku (tidak N+1)

## Fase 7 — Reading Log
- [x] Entity ↔ DTO, validasi aturan bisnis (DONE ⇒ finishedAt, rating ⇒ DONE, finishedAt ≥ startedAt, tidak boleh masa depan)
- [x] `POST/GET /api/books/{bookId}/reading-logs`
- [x] `GET /api/reading-logs?status=`, `GET/PATCH/DELETE /api/reading-logs/{id}`
      (filter status & tenant di-query, bukan di memori → paginasi akurat)
- [x] Test: baca ulang buku yang sama menghasilkan dua log

## Fase 8 — Pengujian End-to-End
- [x] `@SpringBootTest` + Testcontainers: alur penuh (register → login → penerbit → penulis → kategori → buku → log → filter → hapus)
- [ ] Import `bookshelf-api.postman_collection.json`, jalankan semua request berurutan
      — collection belum diperbarui untuk jalur autentikasi (perlu header `Authorization`)
- [x] Cek setiap status code di `API-CONTRACT.md`: 200, 201, 204, 400, 404, 409
- [x] `./mvnw clean verify` hijau dari database kosong (Testcontainers)
- [x] Tulis `README.md`: cara menjalankan + catatan trade-off
- [x] **Selesai fase 1** ✅

## Fase 9 — Prioritas 2 (setelah fase 8 selesai)
- [x] Migration `V2__add_users_and_security.sql` (tabel `users`, kolom `user_id` multi-tenancy)
- [x] Spring Security + JWT (`/api/auth/register|login|refresh|me`), filter token
- [ ] Role `USER` / `ADMIN` pada endpoint tulis vs baca — `User.getAuthorities()` masih `List.of()`
- [ ] `GET /api/books/export?format=csv|json`
- [ ] Update `API-CONTRACT.md`, `TECH-STACK.md`, collection Postman (jalur auth + PATCH baru)

---

## Selisih kontrak yang sudah disesuaikan (30 Sep 2026)
- `PATCH /api/authors|publishers|categories/{id}` ditambahkan — sebelumnya kode hanya punya PUT
- `GET|PATCH|DELETE /api/reading-logs[/{id}]` ditambahkan — sebelumnya hanya ada service-nya
- Pesan 409 ISBN disamakan dengan `API-CONTRACT.md`: `ISBN <isbn> sudah terdaftar`
- `GET /api/books/{id}` kini memeriksa pemilik (menutup kebocoran multi-tenancy)
- Whitelist `/v3/api-docs` & `swagger-ui` dihapus karena tidak ada dependency springdoc
