# TASKS

Kerjakan berurutan. Centang `[x]` saat selesai. Satu fase = satu commit/PR kecil.

## Fase 0 — Persiapan
- [ ] Pasang JDK 21+, verifikasi `java -version`
- [ ] Pasang PostgreSQL 18, buat user + database `bookshelf` (`DEVELOPMENT-SETUP.md` §1)
- [ ] Set environment variable `SPRING_DATASOURCE_*`
- [ ] Simpan seluruh dokumen `.md` di folder `docs/` repo

## Fase 1 — Init Proyek
- [ ] Generate proyek di start.spring.io: Maven, Java 21, Spring Boot 4.1.x, group `dev.bookshelf`, artifact `bookshelf-api`
- [ ] Tambah dependency sesuai `TECH-STACK.md` (webmvc, data-jpa, validation, flyway, flyway-database-postgresql, postgresql, actuator)
- [ ] Inisialisasi git, `.gitignore` (target/, .idea/, `.env`)
- [ ] Buat `application.yml` dan `application-dev.yml`
- [ ] `./mvnw spring-boot:run` → gagal wajar karena belum ada migration (atau start tanpa error jika kosong)

## Fase 2 — Database & Migration
- [ ] Buat `db/migration/V1__init.sql` dari `DATABASE-DESIGN.md` §2
- [ ] Buat `db/seed/V100__seed_dev.sql` dari §5
- [ ] Jalankan aplikasi profil `dev`; cek `flyway_schema_history` dan `\dt` (7 tabel + history)
- [ ] Uji constraint manual di psql: ISBN duplikat ditolak, `rating = 6` ditolak, `DONE` tanpa `finished_at` ditolak
- [ ] Verifikasi `ddl-auto=validate` lolos setelah entity dibuat (Fase 3)

## Fase 3 — Entity & Repository
- [ ] Entity `Author`, `Publisher`, `Category`
- [ ] Entity `Book` (`@ManyToOne Publisher`, `@ManyToMany Set<Author>`, `@ManyToMany Set<Category>`, semua `LAZY`)
- [ ] Entity `ReadingLog` + `enum ReadingStatus`
- [ ] Repository per entity
- [ ] Query filter buku: kategori (slug), penulis, judul, tahun — tanpa N+1 (`@EntityGraph`)
- [ ] `@DataJpaTest` (Testcontainers): simpan buku dengan 2 penulis + 2 kategori, baca ulang, cek relasi

## Fase 4 — Fondasi API
- [ ] `PageResponse<T>` (`content, page, size, totalElements, totalPages`)
- [ ] Exception: `NotFoundException`, `ConflictException`
- [ ] `@RestControllerAdvice` → `ProblemDetail`: 400 (validasi + JSON rusak + tipe parameter), 404, 409, 500
- [ ] Tangani `DataIntegrityViolationException` → 409 (ISBN/nama/slug duplikat, FK RESTRICT)

## Fase 5 — CRUD Master Data
- [ ] Authors: DTO, service, controller, 5 endpoint (`API-CONTRACT.md`)
- [ ] Publishers: idem
- [ ] Categories: idem (validasi pola `slug`)
- [ ] Hapus data yang masih dipakai → 409 dengan pesan jelas
- [ ] Test `@WebMvcTest` per controller (sukses + 400 + 404 + 409)

## Fase 6 — CRUD Buku
- [ ] DTO `BookRequest`, `BookPatchRequest`, `BookResponse` (publisher, authors, categories ter-embed)
- [ ] `POST /api/books` (validasi id publisher/author/category → 404; ISBN duplikat → 409)
- [ ] `GET /api/books/{id}` dan `GET /api/books` + paginasi + sort
- [ ] Filter `?category=`, `?author=`, `?q=`, `?year=`
- [ ] `PATCH /api/books/{id}` (set penulis/kategori = ganti seluruhnya; `isbn: null` mengosongkan)
- [ ] `DELETE /api/books/{id}` (cek cascade ke junction dan log)
- [ ] Verifikasi jumlah query pada list buku (tidak N+1)

## Fase 7 — Reading Log
- [ ] Entity ↔ DTO, validasi aturan bisnis (DONE ⇒ finishedAt, rating ⇒ DONE, finishedAt ≥ startedAt)
- [ ] `POST/GET /api/books/{bookId}/reading-logs`
- [ ] `GET /api/reading-logs?status=`, `GET/PATCH/DELETE /api/reading-logs/{id}`
- [ ] Test: baca ulang buku yang sama menghasilkan dua log

## Fase 8 — Pengujian End-to-End
- [ ] `@SpringBootTest` + Testcontainers: alur penuh (penerbit → penulis → kategori → buku → log → filter → hapus)
- [ ] Import `bookshelf-api.postman_collection.json`, jalankan semua request berurutan
- [ ] Cek setiap status code di `API-CONTRACT.md`: 200, 201, 204, 400, 404, 409
- [ ] `./mvnw clean verify` hijau dari database kosong
- [ ] Tulis `README.md`: cara menjalankan + catatan trade-off `works`/`editions`
- [ ] **Selesai fase 1** ✅

## Fase 9 — Prioritas 2 (setelah fase 8 selesai)
- [ ] Migration `V2__users.sql` (tabel `users`, kolom `role`)
- [ ] Spring Security + JWT (`/api/auth/login`), filter token
- [ ] Role `USER` / `ADMIN` pada endpoint tulis vs baca
- [ ] `GET /api/books/export?format=csv|json`
- [ ] Update `API-CONTRACT.md`, `TECH-STACK.md`, collection Postman
