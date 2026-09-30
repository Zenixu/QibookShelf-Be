# PRD — bookshelf-api

## 1. Tujuan
Membangun backend REST API untuk mencatat koleksi buku pribadi dan riwayat membacanya, sekaligus menjadi sarana belajar arsitektur Spring Boot, JPA, Flyway, dan normalisasi database sampai 3NF.

## 2. Ruang Lingkup
**Termasuk (fase 1):**
- CRUD buku, penulis, penerbit, kategori, dan log membaca
- Relasi banyak-ke-banyak buku–penulis dan buku–kategori
- Filter dan pencarian buku (kategori, penulis, status baca, judul)
- Validasi input dan error terstruktur
- Migration database dengan Flyway

**Termasuk (fase 2):** JWT, role, export.

**Tidak termasuk:**
- Frontend / UI
- Multi-user pada fase 1 (satu pengguna, tanpa tabel `users`)
- Upload sampul buku, integrasi API buku eksternal
- Pemisahan `works`/`editions` (lihat catatan trade-off)

## 3. Pengguna
Satu pemilik rak buku (saya). Diuji lewat Postman/curl.

## 4. User Story
**Katalog**
- US-01: Sebagai pemilik, saya ingin menambah buku dengan judul, ISBN, tahun terbit, penerbit, penulis, dan kategori, agar koleksi tercatat lengkap.
- US-02: Saya ingin melihat daftar buku dengan paginasi dan nama penulis serta kategorinya.
- US-03: Saya ingin memfilter buku berdasarkan kategori (`?category=fiksi`).
- US-04: Saya ingin mengubah sebagian data buku tanpa mengirim ulang semuanya (PATCH).
- US-05: Saya ingin menghapus buku, dan relasi kategori/penulis/log-nya ikut terhapus.
- US-06: Saya ingin ditolak jika memasukkan ISBN yang sudah ada (409).
- US-07: Saya ingin buku bisa punya lebih dari satu penulis.

**Master data**
- US-08: Saya ingin mengelola penulis, penerbit, dan kategori (CRUD).
- US-09: Saya ingin penghapusan penulis/penerbit/kategori yang masih dipakai buku ditolak dengan pesan jelas (409).

**Membaca**
- US-10: Saya ingin mencatat buku masuk wishlist, sedang dibaca, atau selesai.
- US-11: Saya ingin mencatat tanggal mulai, tanggal selesai, dan rating 1–5 untuk setiap peristiwa membaca.
- US-12: Saya ingin membaca ulang buku yang sama (beberapa log per buku).
- US-13: Saya ingin melihat log berdasarkan status.

**Fase 2**
- US-14: Saya ingin login dan mengakses API dengan JWT.
- US-15: Saya ingin peran `ADMIN` dan `USER` dengan hak akses berbeda.
- US-16: Saya ingin mengekspor koleksi ke CSV/JSON.

## 5. Fitur

### Prioritas 1 — CRUD + filter kategori
| ID | Fitur |
|---|---|
| F1 | CRUD `authors`, `publishers`, `categories` |
| F2 | CRUD `books` (termasuk set penulis & kategori) |
| F3 | Filter `GET /api/books?category={slug}`, plus `author`, `q`, paginasi |
| F4 | CRUD `reading_logs`, filter `status` |
| F5 | Validasi + error `ProblemDetail` (400/404/409) |
| F6 | Flyway `V1__init.sql` + seed dev |
| F7 | Test integrasi dengan PostgreSQL asli (Testcontainers) |

### Prioritas 2 — setelah CRUD berjalan
| ID | Fitur |
|---|---|
| F8 | Spring Security + JWT (login, refresh opsional) |
| F9 | Role `USER` / `ADMIN` (tabel `users` ditambah lewat migration baru) |
| F10 | Export buku ke CSV & JSON |

## 6. Kriteria Keberhasilan
- Semua endpoint di `API-CONTRACT.md` berfungsi dan lolos test.
- `V1__init.sql` berjalan dari database kosong tanpa error.
- Tidak ada kolom berisi list dipisah koma; skema memenuhi 1NF–3NF.
- Seluruh alur bisa dicoba dari Postman.

## 7. Catatan Trade-off (keputusan yang disengaja)
Judul karya bisa terulang jika buku yang sama diterbitkan ulang oleh penerbit berbeda (edisi berbeda), sehingga ada *update anomaly* ringan pada `books`. Solusi sempurnanya adalah memisah menjadi tabel `works` (judul + penulis) dan `editions` (penerbit + tahun + ISBN). Untuk proyek percobaan ini hal tersebut **sengaja tidak dilakukan** demi menjaga kesederhanaan. Ini keputusan desain, bukan celah yang terlewat.
