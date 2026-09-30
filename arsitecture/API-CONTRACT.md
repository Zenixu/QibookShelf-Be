# API-CONTRACT

Base URL: `http://localhost:8080`  ·  Prefix: `/api`  ·  Format: `application/json` (UTF-8)
Fase 1 tanpa autentikasi. Fase 2 menambah header `Authorization: Bearer <jwt>`.

## Konvensi

**Nama field JSON:** `camelCase`. **Tanggal:** `YYYY-MM-DD`.

**Status code umum**
| Kode | Arti |
|---|---|
| 200 | Sukses (GET, PATCH, PUT) |
| 201 | Dibuat (POST), dengan header `Location` |
| 204 | Dihapus, tanpa body |
| 400 | Validasi gagal / JSON tidak valid / parameter salah |
| 404 | Data tidak ditemukan |
| 409 | Konflik: ISBN/nama/slug duplikat, atau data masih dipakai |
| 500 | Kesalahan server |

**Format error** (RFC 9457 `ProblemDetail`, `Content-Type: application/problem+json`)
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "ISBN 9789793062792 sudah terdaftar",
  "instance": "/api/books"
}
```
Untuk 400 validasi, ada tambahan `errors`:
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validasi gagal",
  "instance": "/api/books",
  "errors": [
    { "field": "title", "message": "tidak boleh kosong" },
    { "field": "publishYear", "message": "harus antara 1000 dan 2100" }
  ]
}
```

**Paginasi** (semua endpoint list): query `page` (mulai 0, default 0), `size` (default 20, maks 100), `sort` (mis. `title,asc`).
```json
{
  "content": [ ],
  "page": 0,
  "size": 20,
  "totalElements": 4,
  "totalPages": 1
}
```

## Ringkasan Endpoint
| Method | Path | Fungsi | Sukses |
|---|---|---|---|
| POST | `/api/authors` | Buat penulis | 201 |
| GET | `/api/authors` | Daftar (`?q=`) | 200 |
| GET | `/api/authors/{id}` | Detail | 200 |
| PATCH | `/api/authors/{id}` | Ubah sebagian | 200 |
| DELETE | `/api/authors/{id}` | Hapus (409 jika dipakai) | 204 |
| POST/GET/PATCH/DELETE | `/api/publishers[/{id}]` | Sama seperti authors | |
| POST/GET/PATCH/DELETE | `/api/categories[/{id}]` | Sama seperti authors | |
| POST | `/api/books` | Buat buku | 201 |
| GET | `/api/books` | Daftar + filter | 200 |
| GET | `/api/books/{id}` | Detail | 200 |
| PATCH | `/api/books/{id}` | Ubah sebagian | 200 |
| DELETE | `/api/books/{id}` | Hapus (cascade relasi & log) | 204 |
| POST | `/api/books/{bookId}/reading-logs` | Buat log baca | 201 |
| GET | `/api/books/{bookId}/reading-logs` | Log untuk satu buku | 200 |
| GET | `/api/reading-logs` | Semua log (`?status=`) | 200 |
| GET | `/api/reading-logs/{id}` | Detail log | 200 |
| PATCH | `/api/reading-logs/{id}` | Ubah log | 200 |
| DELETE | `/api/reading-logs/{id}` | Hapus log | 204 |

---

## Authors
### POST `/api/authors`
Request:
```json
{ "name": "Andrea Hirata", "nationality": "Indonesia" }
```
Validasi: `name` wajib, 1–150 karakter; `nationality` opsional, maks 80.

Response `201`:
```json
{ "id": 1, "name": "Andrea Hirata", "nationality": "Indonesia" }
```
Error: `400`.

### GET `/api/authors?q=andrea&page=0&size=20`
Response `200`: `PageResponse` berisi objek author (`q` = pencarian nama, *case-insensitive*, sebagian).

### GET `/api/authors/{id}` → `200` / `404`
### PATCH `/api/authors/{id}`
Request (semua field opsional):
```json
{ "nationality": "Indonesia" }
```
Response `200`: objek author terbaru. Error: `400`, `404`.

### DELETE `/api/authors/{id}`
`204`. Jika masih menulis buku:
```json
{ "type": "about:blank", "title": "Conflict", "status": 409,
  "detail": "Penulis id=1 masih dipakai oleh 2 buku", "instance": "/api/authors/1" }
```

## Publishers
Body `POST`: `{ "name": "Bentang Pustaka", "city": "Yogyakarta" }`. `name` wajib, unik (409 jika duplikat). Response: `{ "id": 1, "name": "...", "city": "..." }`.

## Categories
Body `POST`: `{ "name": "Fiksi", "slug": "fiksi" }`. `name` dan `slug` wajib dan unik (409). `slug` harus cocok `^[a-z0-9]+(-[a-z0-9]+)*$`. Response: `{ "id": 1, "name": "Fiksi", "slug": "fiksi" }`.

---

## Books

### POST `/api/books`
Request:
```json
{
  "title": "Laskar Pelangi",
  "isbn": "9789793062792",
  "publishYear": 2005,
  "publisherId": 1,
  "authorIds": [1],
  "categoryIds": [1, 4]
}
```
| Field | Aturan |
|---|---|
| `title` | wajib, 1–255 |
| `isbn` | opsional; `^([0-9]{9}[0-9X]\|[0-9]{13})$`, tanpa tanda hubung; unik |
| `publishYear` | opsional; 1000–2100 |
| `publisherId` | wajib; harus ada |
| `authorIds` | wajib, minimal 1 id, semua harus ada |
| `categoryIds` | opsional (boleh kosong), semua harus ada |

Response `201` + `Location: /api/books/1`:
```json
{
  "id": 1,
  "title": "Laskar Pelangi",
  "isbn": "9789793062792",
  "publishYear": 2005,
  "publisher": { "id": 1, "name": "Bentang Pustaka", "city": "Yogyakarta" },
  "authors": [ { "id": 1, "name": "Andrea Hirata" } ],
  "categories": [
    { "id": 1, "name": "Fiksi", "slug": "fiksi" },
    { "id": 4, "name": "Sastra", "slug": "sastra" }
  ]
}
```
Error:
- `400` — validasi gagal
- `404` — `publisherId` / id penulis / id kategori tidak ada
- `409` — ISBN duplikat:
```json
{ "type": "about:blank", "title": "Conflict", "status": 409,
  "detail": "ISBN 9789793062792 sudah terdaftar", "instance": "/api/books" }
```

### GET `/api/books`
Query:
| Param | Contoh | Arti |
|---|---|---|
| `category` | `fiksi` | Filter slug kategori |
| `author` | `1` | Filter id penulis |
| `q` | `laskar` | Cari judul (*case-insensitive*, sebagian) |
| `year` | `2005` | Filter tahun terbit |
| `page`, `size`, `sort` | `0`, `20`, `title,asc` | Paginasi & urutan |

Contoh: `GET /api/books?category=fiksi&page=0&size=10`

Response `200`:
```json
{
  "content": [
    {
      "id": 1,
      "title": "Laskar Pelangi",
      "isbn": "9789793062792",
      "publishYear": 2005,
      "publisher": { "id": 1, "name": "Bentang Pustaka", "city": "Yogyakarta" },
      "authors": [ { "id": 1, "name": "Andrea Hirata" } ],
      "categories": [
        { "id": 1, "name": "Fiksi", "slug": "fiksi" },
        { "id": 4, "name": "Sastra", "slug": "sastra" }
      ]
    },
    {
      "id": 2,
      "title": "Bumi Manusia",
      "isbn": "9789799731234",
      "publishYear": 2005,
      "publisher": { "id": 2, "name": "Lentera Dipantara", "city": "Jakarta" },
      "authors": [ { "id": 2, "name": "Pramoedya Ananta Toer" } ],
      "categories": [
        { "id": 1, "name": "Fiksi", "slug": "fiksi" },
        { "id": 2, "name": "Sejarah", "slug": "sejarah" }
      ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 2,
  "totalPages": 1
}
```
Catatan: filter `category` hanya menyaring **buku mana yang tampil**; setiap buku tetap menampilkan **semua** kategori dan penulisnya. Slug yang tidak ada → `200` dengan `content: []` (bukan 404). Parameter salah tipe (mis. `year=abc`) → `400`.

### GET `/api/books/{id}` → `200` (bentuk sama seperti item di atas) / `404`

### PATCH `/api/books/{id}`
Semua field opsional; hanya field yang dikirim yang berubah. Jika `authorIds` / `categoryIds` dikirim, **menggantikan seluruh set** (bukan menambah). Kirim `"isbn": null` untuk mengosongkan ISBN.
```json
{ "publishYear": 2006, "categoryIds": [1] }
```
Response `200`: objek buku terbaru. Error: `400`, `404`, `409` (ISBN duplikat). `authorIds: []` → `400`.

### DELETE `/api/books/{id}`
`204`. Baris `book_authors`, `book_categories`, dan `reading_logs` ikut terhapus. `404` jika tidak ada.

---

## Reading Logs

### POST `/api/books/{bookId}/reading-logs`
Request:
```json
{ "status": "DONE", "startedAt": "2026-01-05", "finishedAt": "2026-01-20", "rating": 5 }
```
| Field | Aturan |
|---|---|
| `status` | wajib: `WISHLIST` \| `READING` \| `DONE` |
| `startedAt`, `finishedAt` | opsional; `finishedAt >= startedAt` |
| `rating` | opsional; 1–5; hanya boleh jika `status = DONE` |
| — | `status = DONE` mewajibkan `finishedAt` |

Response `201`:
```json
{
  "id": 1,
  "bookId": 1,
  "status": "DONE",
  "startedAt": "2026-01-05",
  "finishedAt": "2026-01-20",
  "rating": 5
}
```
Error: `400`, `404` (buku tidak ada).

### GET `/api/reading-logs?status=READING`
Response `200`: `PageResponse` berisi log, masing-masing dengan `bookId` dan `bookTitle`. `status` tidak valid → `400`.

### GET `/api/books/{bookId}/reading-logs` → `200` (array log urut terbaru) / `404`
### GET `/api/reading-logs/{id}` → `200` / `404`
### PATCH `/api/reading-logs/{id}`
```json
{ "status": "DONE", "finishedAt": "2026-09-28", "rating": 4 }
```
Aturan bisnis divalidasi terhadap **keadaan akhir** log (bukan hanya field yang dikirim). Error: `400`, `404`.
### DELETE `/api/reading-logs/{id}` → `204` / `404`

---

## Fase 2 (garis besar)
| Method | Path | Fungsi |
|---|---|---|
| POST | `/api/auth/login` | `{username,password}` → `{accessToken, tokenType, expiresIn}` |
| GET | `/api/books/export?format=csv\|json` | Ekspor koleksi (`ADMIN`/`USER`) |

Kode tambahan: `401` (token tidak ada/tidak valid), `403` (role tidak cukup).
