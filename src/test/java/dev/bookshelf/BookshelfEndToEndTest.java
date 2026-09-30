package dev.bookshelf;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import dev.bookshelf.readinglog.ReadingLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Fase 8 — uji end-to-end alur lengkap API melewati seluruh lapisan
 * (controller → service → repository → PostgreSQL di Testcontainers).
 * Kontainer dimulai dari database kosong, lalu alur kerja realistis
 * dijalankan secara berurutan: penerbit → penulis → kategori → buku →
 * log baca → filter → hapus.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookshelfEndToEndTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReadingLogRepository readingLogRepository;

    private static Long publisherId;
    private static Long authorId;
    private static Long authorLainId;
    private static Long categoryId;
    private static Long categoryLainId;
    private static Long bookId;
    private static Long bookLainId;
    private static Long logId;

    /** Access token hasil login pada tes @Order(0); dipakai seluruh tes berikutnya. */
    private static String accessToken;

    // ===== bantuan =====

    /**
     * Menyuntikkan header Authorization pada setiap request yang dibuat lewat
     * helper get/post/patch/delete di bawah. Register & login berjalan sebelum
     * token ada, jadi saat itu header dilewati (endpoint-nya memang public).
     */
    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder withAuth(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder) {
        return accessToken == null
                ? builder
                : builder.header("Authorization", "Bearer " + accessToken);
    }

    // Metode-metode ini disengaja namanya sama dengan MockMvcRequestBuilders:
    // method kelas menaungi static import on-demand, sehingga SEMUA pemanggilan
    // get(...)/post(...)/patch(...)/delete(...) di tes ini otomatis ter-autentikasi.

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder get(String path) {
        return withAuth(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(path));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder post(String path) {
        return withAuth(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder patch(String path) {
        return withAuth(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(path));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder delete(String path) {
        return withAuth(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(path));
    }

    private MvcResult post(String path, Object body) throws Exception {
        return mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    private void assertStatus(MvcResult result, int expected) throws Exception {
        assertThat(result.getResponse().getStatus())
                .as("body: %s", result.getResponse().getContentAsString())
                .isEqualTo(expected);
    }

    private MvcResult patch(String path, Object body) throws Exception {
        return mockMvc.perform(patch(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private Long idOf(MvcResult result) throws Exception {
        return json(result).get("id").asLong();
    }

    // ===== 0. autentikasi =====

    @Test
    @Order(0)
    @DisplayName("POST /api/auth/register lalu /login → dapat access token")
    void daftarDanLogin() throws Exception {
        MvcResult register = post("/api/auth/register", Map.of(
                "email", "e2e@qibook.test",
                "username", "e2euser",
                "password", "password123",
                "fullName", "E2E Tester"));
        assertStatus(register, 200);

        accessToken = json(register).get("accessToken").asText();
        assertThat(accessToken).isNotBlank();

        MvcResult me = mockMvc.perform(get("/api/auth/me"))
                .andReturn();
        assertStatus(me, 200);
        assertThat(json(me).get("username").asText()).isEqualTo("e2euser");
    }

    // ===== 1. master data =====

    @Test
    @Order(1)
    @DisplayName("POST publisher/author/category → 201 dan dapat id")
    void buatMasterData() throws Exception {
        MvcResult publisher = post("/api/publishers",
                Map.of("name", "Gramedia", "city", "Jakarta"));
        assertStatus(publisher, 201);
        publisherId = idOf(publisher);
        assertThat(json(publisher).get("name").asText()).isEqualTo("Gramedia");

        MvcResult author = post("/api/authors",
                Map.of("name", "Tere Liye", "nationality", "Indonesia"));
        assertStatus(author, 201);
        authorId = idOf(author);

        MvcResult author2 = post("/api/authors",
                Map.of("name", "Pramoedya", "nationality", "Indonesia"));
        authorLainId = idOf(author2);

        MvcResult category = post("/api/categories",
                Map.of("name", "Fiksi", "slug", "fiksi"));
        assertThat(json(category).get("slug").asText()).isEqualTo("fiksi");
        categoryId = idOf(category);

        categoryLainId = idOf(post("/api/categories",
                Map.of("name", "Sejarah", "slug", "sejarah")));
    }

    @Test
    @Order(2)
    @DisplayName("POST category slug tidak valid → 400")
    void slugTidakValidDitolak() throws Exception {
        MvcResult result = post("/api/categories",
                Map.of("name", "Fiksi Remaja", "slug", "Fiksi Remaja!"));
        assertStatus(result, 400);
        assertThat(json(result).get("title").asText()).isEqualTo("Bad Request");
    }

    @Test
    @Order(3)
    @DisplayName("POST author nama duplikat → 409")
    void namaDuplikatDitolak() throws Exception {
        MvcResult result = post("/api/authors",
                Map.of("name", "Tere Liye", "nationality", "Indonesia"));
        assertStatus(result, 409);
        assertThat(json(result).get("detail").asText())
                .contains("sudah terdaftar");
    }

    @Test
    @Order(4)
    @DisplayName("POST book dengan 2 penulis + 2 kategori → 201")
    void buatBukuLengkap() throws Exception {
        MvcResult result = post("/api/books", Map.of(
                "title", "Hujan",
                "isbn", "9786020324123",
                "publishYear", 2016,
                "publisherId", publisherId,
                "authorIds", java.util.List.of(authorId, authorLainId),
                "categoryIds", java.util.List.of(categoryId, categoryLainId)
        ));
        assertStatus(result, 201);
        bookId = idOf(result);

        JsonNode body = json(result);
        assertThat(body.get("title").asText()).isEqualTo("Hujan");
        assertThat(body.get("publisher").get("name").asText()).isEqualTo("Gramedia");
        assertThat(body.get("authors")).hasSize(2);
        assertThat(body.get("categories")).hasSize(2);
    }

    @Test
    @Order(5)
    @DisplayName("GET book by id → 200 dengan seluruh relasi tertanam")
    void ambilBukuById() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = json(result);
        assertThat(body.get("isbn").asText()).isEqualTo("9786020324123");
        assertThat(body.get("authors")).hasSize(2);
        assertThat(body.get("categories")).hasSize(2);
    }

    @Test
    @Order(6)
    @DisplayName("GET book id tidak ada → 404 ProblemDetail")
    void bukuTidakAda() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books/99999"))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(json(result).get("title").asText()).isEqualTo("Not Found");
    }

    @Test
    @Order(7)
    @DisplayName("POST book ISBN duplikat → 409")
    void isbnDuplikatDitolak() throws Exception {
        MvcResult result = post("/api/books", Map.of(
                "title", "Buku Duplikat",
                "isbn", "9786020324123",
                "publishYear", 2020,
                "publisherId", publisherId
        ));
        assertStatus(result, 409);
        assertThat(json(result).get("detail").asText())
                .contains("9786020324123 sudah terdaftar");
    }

    @Test
    @Order(8)
    @DisplayName("POST book publisher tidak ada → 404")
    void publisherTidakAda() throws Exception {
        MvcResult result = post("/api/books", Map.of(
                "title", "Buku Lengkap",
                "isbn", "9780000000011",
                "publishYear", 2021,
                "publisherId", 99999
        ));
        assertStatus(result, 404);
    }

    @Test
    @Order(9)
    @DisplayName("POST book tanpa judul → 400 dengan daftar errors")
    void validasiBukuGagal() throws Exception {
        MvcResult result = post("/api/books", Map.of(
                "isbn", "9780000000012",
                "publishYear", 2021,
                "publisherId", publisherId
        ));
        assertStatus(result, 400);
        JsonNode errors = json(result).get("errors");
        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0).get("field").asText()).isEqualTo("title");
    }

    // ===== 10. filter & paginasi =====

    @Test
    @Order(10)
    @DisplayName("Buku kedua lalu filter ?category=fiksi → hanya buku fiksi, relasi tetap utuh")
    void filterBerdasarkanKategori() throws Exception {
        MvcResult result = post("/api/books", Map.of(
                "title", "Bumi Manusia",
                "isbn", "9789799731234",
                "publishYear", 1980,
                "publisherId", publisherId,
                "authorIds", java.util.List.of(authorLainId),
                "categoryIds", java.util.List.of(categoryLainId)
        ));
        assertStatus(result, 201);
        bookLainId = idOf(result);

        MvcResult filtered = mockMvc.perform(
                        get("/api/books").param("category", "fiksi"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = json(filtered);
        assertThat(body.get("content")).hasSize(1);
        JsonNode bukuFiksi = body.get("content").get(0);
        assertThat(bukuFiksi.get("title").asText()).isEqualTo("Hujan");
        // kategori tetap utuh meski difilter per kategori
        assertThat(bukuFiksi.get("categories")).hasSize(2);
    }

    @Test
    @Order(11)
    @DisplayName("Filter ?q=hujan cocok judul tanpa case-sensitive")
    void filterJudul() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books").param("q", "hujan"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json(result).get("content")).hasSize(1);
        assertThat(json(result).get("content").get(0).get("title").asText())
                .isEqualTo("Hujan");
    }

    @Test
    @Order(12)
    @DisplayName("Filter ?year=1980 membatasi tahun terbit")
    void filterTahun() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books").param("year", "1980"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json(result).get("content")).hasSize(1);
    }

    @Test
    @Order(13)
    @DisplayName("Filter ?author=<id> membatasi penulis")
    void filterPenulis() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/books").param("author", String.valueOf(authorId)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json(result).get("content")).hasSize(1);
        assertThat(json(result).get("content").get(0).get("title").asText())
                .isEqualTo("Hujan");
    }

    @Test
    @Order(14)
    @DisplayName("Paginasi ?size=1&page=0 → 1 item per halaman, totalPages 2")
    void paginasi() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/books").param("size", "1").param("page", "0"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = json(result);
        assertThat(body.get("content")).hasSize(1);
        assertThat(body.get("totalPages").asInt()).isEqualTo(2);
        assertThat(body.get("totalElements").asInt()).isEqualTo(2);
        assertThat(body.get("page").asInt()).isZero();
        assertThat(body.get("size").asInt()).isEqualTo(1);
    }

    @Test
    @Order(15)
    @DisplayName("Sort ?sort=publishYear,desc → buku terbaru lebih dulu")
    void sortBuku() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/books").param("sort", "publishYear,desc"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode content = json(result).get("content");
        assertThat(content.get(0).get("publishYear").asInt()).isEqualTo(2016);
        assertThat(content.get(1).get("publishYear").asInt()).isEqualTo(1980);
    }

    // ===== 16. patch parsial =====

    @Test
    @Order(16)
    @DisplayName("PATCH hanya mengubah field yang dikirim")
    void patchParsial() throws Exception {
        MvcResult result = patch("/api/books/" + bookId, Map.of(
                "title", "Hujan (Edisi Revisi)",
                "categoryIds", java.util.List.of(categoryId)  // ganti total kategori
        ));
        assertStatus(result, 200);
        JsonNode body = json(result);
        assertThat(body.get("title").asText()).isEqualTo("Hujan (Edisi Revisi)");
        // kategori menyusut jadi 1 (ganti seluruhnya), isbn tak berubah
        assertThat(body.get("categories")).hasSize(1);
        assertThat(body.get("isbn").asText()).isEqualTo("9786020324123");
    }

    // ===== 17. reading log =====

    @Test
    @Order(17)
    @DisplayName("POST reading-log DONE dengan rating → 201")
    void buatLogDone() throws Exception {
        MvcResult result = post("/api/books/" + bookId + "/reading-logs", Map.of(
                "status", "DONE",
                "startedAt", "2026-01-05",
                "finishedAt", "2026-01-20",
                "rating", 5
        ));
        assertStatus(result, 201);
        logId = idOf(result);
        JsonNode body = json(result);
        assertThat(body.get("status").asText()).isEqualTo("DONE");
        assertThat(body.get("rating").asInt()).isEqualTo(5);
        assertThat(body.get("bookTitle").asText()).isEqualTo("Hujan (Edisi Revisi)");
    }

    @Test
    @Order(18)
    @DisplayName("POST reading-log rating saat READING → 409")
    void ratingBukanDoneDitolak() throws Exception {
        MvcResult result = post("/api/books/" + bookId + "/reading-logs", Map.of(
                "status", "READING",
                "startedAt", "2026-02-01",
                "rating", 4
        ));
        assertStatus(result, 409);
        assertThat(json(result).get("detail").asText())
                .contains("Rating hanya boleh diisi pada status DONE");
    }

    @Test
    @Order(19)
    @DisplayName("POST DONE tanpa finishedAt → 409")
    void doneWajibFinishedAt() throws Exception {
        MvcResult result = post("/api/books/" + bookId + "/reading-logs", Map.of(
                "status", "DONE",
                "startedAt", "2026-02-01"
        ));
        assertStatus(result, 409);
    }

    @Test
    @Order(20)
    @DisplayName("POST finishedAt sebelum startedAt → 409")
    void urutanTanggalSalah() throws Exception {
        MvcResult result = post("/api/books/" + bookId + "/reading-logs", Map.of(
                "status", "DONE",
                "startedAt", "2026-03-10",
                "finishedAt", "2026-03-01",
                "rating", 3
        ));
        assertStatus(result, 409);
        assertThat(json(result).get("detail").asText())
                .contains("finishedAt tidak boleh sebelum startedAt");
    }

    @Test
    @Order(21)
    @DisplayName("POST tanggal masa depan → 409")
    void tanggalMasaDepan() throws Exception {
        MvcResult result = post("/api/books/" + bookId + "/reading-logs", Map.of(
                "status", "READING",
                "startedAt", LocalDate.now().plusYears(1).toString()
        ));
        assertStatus(result, 409);
    }

    @Test
    @Order(22)
    @DisplayName("POST reading-log ke buku tidak ada → 404")
    void logKeBukuTidakAda() throws Exception {
        MvcResult result = post("/api/books/99999/reading-logs", Map.of(
                "status", "WISHLIST"
        ));
        assertStatus(result, 404);
    }

    @Test
    @Order(23)
    @DisplayName("GET reading-logs per buku urut terbaru → 200")
    void logPerBukuUrut() throws Exception {
        post("/api/books/" + bookId + "/reading-logs", Map.of(
                "status", "READING",
                "startedAt", "2026-09-01"
        ));
        MvcResult result = mockMvc.perform(
                        get("/api/books/" + bookId + "/reading-logs"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = json(result);
        assertThat(body).hasSize(2);
        assertThat(body.get(0).get("status").asText()).isEqualTo("READING");
        assertThat(body.get(1).get("status").asText()).isEqualTo("DONE");
    }

    @Test
    @Order(24)
    @DisplayName("GET /api/reading-logs?status=DONE memfilter dengan benar")
    void filterStatusLog() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/reading-logs").param("status", "DONE"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode content = json(result).get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("status").asText()).isEqualTo("DONE");
    }

    @Test
    @Order(25)
    @DisplayName("GET /api/reading-logs status tidak valid → 400")
    void statusTidakValid() throws Exception {
        mockMvc.perform(get("/api/reading-logs").param("status", "BACA"))
                .andExpect(status().isBadRequest());
    }

    // ===== 26. pelindung relasi =====

    @Test
    @Order(26)
    @DisplayName("DELETE author yang masih dipakai buku → 409")
    void hapusAuthorTerpakai() throws Exception {
        MvcResult result = mockMvc.perform(delete("/api/authors/" + authorId))
                .andReturn();
        assertStatus(result, 409);
        assertThat(json(result).get("detail").asText()).contains("masih dipakai");
    }

    // ===== 27. hapus berantai =====

    @Test
    @Order(27)
    @DisplayName("DELETE book → 204, lalu 404; junction & log turut bersih")
    void hapusBukuBerantai() throws Exception {
        mockMvc.perform(delete("/api/books/" + bookId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(status().isNotFound());

        // reading-logs buku ini ikut terhapus (FK ON DELETE CASCADE)
        assertThat(readingLogRepository.findByBookIdOrderByStartedAtDesc(bookId))
                .isEmpty();
    }

    @Test
    @Order(28)
    @DisplayName("GET /api/books setelah penghapusan menyisakan 1 buku")
    void sisaBuku() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json(result).get("totalElements").asInt()).isEqualTo(1);
        assertThat(json(result).get("content").get(0).get("title").asText())
                .isEqualTo("Bumi Manusia");
    }

    @Test
    @Order(29)
    @DisplayName("DELETE publisher sekarang bisa (tidak ada buku tersisa yang memakainya)")
    void hapusPublisherSetelahBukuHabis() throws Exception {
        // buku terakhir ("Bumi Manusia") masih memakai publisher yang sama
        mockMvc.perform(delete("/api/books/" + bookLainId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/publishers/" + publisherId))
                .andExpect(status().isNoContent());
    }
}
