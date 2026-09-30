# AGENT.md — Panduan untuk AI Agent / Kontributor

Baca file ini sebelum mengubah apa pun di `bookshelf-api`.

## Ringkasan Proyek
Backend REST API pencatat buku pribadi. Tujuan utama: belajar Spring Boot, JPA, Flyway, dan normalisasi DB (3NF). **Backend saja** — jangan buat atau bahas frontend. Pengujian manual memakai Postman/curl.

## Dokumen Rujukan (sumber kebenaran)
| File | Isi |
|---|---|
| `PRD.md` | Tujuan, ruang lingkup, user story, prioritas |
| `DATABASE-DESIGN.md` | Skema final, DDL, seed |
| `API-CONTRACT.md` | Endpoint, request/response, status code |
| `TECH-STACK.md` | Dependency |
| `DEVELOPMENT-SETUP.md` | Cara menjalankan (terminal/Maven) |
| `INTELLIJ-SETUP.md` | Cara menjalankan & debug di IntelliJ IDEA |
| `TASKS.md` | Urutan kerja — kerjakan berurutan, centang saat selesai |

Jika kode dan dokumen bertentangan, hentikan dan tanyakan; jangan diam-diam mengubah salah satunya.

## Stack
Java 21+, Spring Boot 4.1.x, Maven Wrapper (`./mvnw`), PostgreSQL 18 (`localhost:5432`, DB `bookshelf`), Flyway, Spring Data JPA/Hibernate, Bean Validation. JWT = fase 2.

## Perintah
```bash
./mvnw clean verify        # build + test
./mvnw spring-boot:run     # jalankan (profil dev)
./mvnw test                # test saja
```

## Aturan Wajib
1. **Skema DB final. Jangan diubah** (tabel, kolom, relasi). Perubahan hanya lewat persetujuan eksplisit dari pemilik proyek.
2. **Skema hanya berubah lewat Flyway.** Buat file baru `V{n}__deskripsi.sql`. **Jangan pernah mengedit migration yang sudah dijalankan.**
3. `spring.jpa.hibernate.ddl-auto=validate` di semua profil. Dilarang `update` / `create`.
4. **Jangan ekspos entity JPA di controller.** Pakai DTO (Java `record`) untuk request dan response.
5. Dilarang menyimpan list dipisah koma dalam satu kolom (1NF). Relasi many-to-many lewat junction table.
6. Status `reading_logs` dibatasi CHECK di DB dan `enum` di Java (`WISHLIST`, `READING`, `DONE`). Bukan tabel.
7. Kredensial DB hanya dari environment variable (`SPRING_DATASOURCE_*`). Jangan hardcode, jangan commit.
8. Trade-off `works`/`editions` adalah **keputusan yang disengaja**. Jangan "memperbaikinya" tanpa diminta.
9. Ruang lingkup: jangan tambah tabel `users` sebelum fase 2.

## Konvensi Kode
- Package dasar: `dev.bookshelf`. Struktur per-fitur, bukan per-layer:
  ```
  dev.bookshelf
  ├── book/         (BookController, BookService, BookRepository, Book, dto/)
  ├── author/
  ├── publisher/
  ├── category/
  ├── readinglog/
  └── common/       (exception/, PageResponse, config)
  ```
- Alur: `Controller → Service → Repository`. Logika bisnis hanya di Service. `@Transactional` di Service.
- Relasi many-to-many: `@ManyToMany` + `@JoinTable` di sisi `Book`, `Set<>` bukan `List<>`. Semua asosiasi `LAZY`. Hindari N+1 (`@EntityGraph` atau fetch join untuk list buku).
- Validasi di DTO (`@NotBlank`, `@Size`, `@Min`/`@Max`, `@Pattern`). Controller pakai `@Valid`.
- Error dikonversi terpusat di `@RestControllerAdvice` ke format RFC 9457 (`ProblemDetail`). Lihat `API-CONTRACT.md`.
- Penamaan: tabel/kolom `snake_case`, class `PascalCase`, JSON `camelCase`, path URL `kebab-case` jamak.
- Tanpa Lombok. Pakai `record` untuk DTO, konstruktor/getter biasa untuk entity.
- Komentar dan dokumen: Bahasa Indonesia. Nama identifier: Inggris.

## Definisi Selesai (per tugas)
- [ ] Kode kompilasi, `./mvnw verify` hijau
- [ ] Sesuai `API-CONTRACT.md` (path, status code, bentuk JSON)
- [ ] Ada test untuk jalur sukses dan minimal satu jalur error
- [ ] Migration baru (jika ada) berurutan dan bisa jalan dari DB kosong
- [ ] Centang item di `TASKS.md`

## Cara Bekerja
- Satu tugas `TASKS.md` per langkah. Tunjukkan rencana singkat sebelum mengubah banyak file.
- Jika persyaratan ambigu, ajukan satu pertanyaan spesifik, jangan menebak.
- Jangan menambah dependency di luar `TECH-STACK.md` tanpa alasan tertulis.
