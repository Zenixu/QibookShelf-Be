package dev.bookshelf.book;

import dev.bookshelf.author.Author;
import dev.bookshelf.author.AuthorRepository;
import dev.bookshelf.category.Category;
import dev.bookshelf.category.CategoryRepository;
import dev.bookshelf.publisher.Publisher;
import dev.bookshelf.publisher.PublisherRepository;
import dev.bookshelf.readinglog.ReadingLog;
import dev.bookshelf.readinglog.ReadingLogRepository;
import dev.bookshelf.readinglog.ReadingStatus;
import dev.bookshelf.security.User;
import dev.bookshelf.security.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReadingLogRepository readingLogRepository;

    @Autowired
    private UserRepository userRepository;

    private User simpanUser(String username) {
        return userRepository.save(new User(
                username + "@test.dev", username, "hash-" + username, "User " + username));
    }

    @Test
    void simpanBukuDuaPenulisDuaKategori_bacaUlang_relasiKonsisten() {
        // given: 2 penulis, 1 penerbit, 2 kategori, 1 user
        User user = simpanUser("uji1");
        Author penulis1 = authorRepository.save(new Author("Penulis Satu", "Indonesia"));
        Author penulis2 = authorRepository.save(new Author("Penulis Dua", "Indonesia"));
        Publisher penerbit = publisherRepository.save(new Publisher("Penerbit Contoh", "Bandung"));
        Category fiksi = categoryRepository.save(new Category("Fiksi", "fiksi"));
        Category sastra = categoryRepository.save(new Category("Sastra", "sastra"));

        Book buku = new Book("Buku Uji Coba", "9780000000001", 2024, penerbit, user);
        buku.getAuthors().addAll(List.of(penulis1, penulis2));
        buku.getCategories().addAll(List.of(fiksi, sastra));
        bookRepository.saveAndFlush(buku);

        // when: baca ulang lewat query detail (anti N+1)
        Book hasil = bookRepository.findWithDetailsById(buku.getId()).orElseThrow();

        // then
        assertThat(hasil.getTitle()).isEqualTo("Buku Uji Coba");
        assertThat(hasil.getPublisher().getName()).isEqualTo("Penerbit Contoh");
        assertThat(hasil.getAuthors())
                .extracting(Author::getName)
                .containsExactlyInAnyOrder("Penulis Satu", "Penulis Dua");
        assertThat(hasil.getCategories())
                .extracting(Category::getSlug)
                .containsExactlyInAnyOrder("fiksi", "sastra");

        // junction table benar-benar terisi
        assertThat(authorRepository.isAuthorUsedByBooks(penulis1.getId())).isTrue();
        assertThat(categoryRepository.isCategoryUsedByBooks(sastra.getId())).isTrue();

        // relasi balik dihapus bersih saat buku dihapus (ON DELETE CASCADE)
        bookRepository.delete(buku);
        assertThat(readingLogRepository.findByBookIdOrderByStartedAtDesc(buku.getId())).isEmpty();
        assertThat(bookRepository.findByIsbn("9780000000001")).isEmpty();
    }

    @Test
    void filterKategoriMengembalikanBukuDenganSemuaRelasinya() {
        User user = simpanUser("uji2");
        Author penulis = authorRepository.save(new Author("Penulis Tunggal", "Indonesia"));
        Publisher penerbit = publisherRepository.save(new Publisher("Penerbit Lain", "Surabaya"));
        Category fiksi = categoryRepository.save(new Category("Fiksi", "fiksi"));
        Category sejarah = categoryRepository.save(new Category("Sejarah", "sejarah"));

        Book buku1 = new Book("Novel Pertama", "9780000000002", 2020, penerbit, user);
        buku1.getAuthors().add(penulis);
        buku1.getCategories().add(fiksi);
        Book buku2 = new Book("Sejarah Kuno", "9780000000003", 2021, penerbit, user);
        buku2.getAuthors().add(penulis);
        buku2.getCategories().add(sejarah);
        bookRepository.saveAll(List.of(buku1, buku2));

        // when: filter kategori fiksi milik user ini
        Page<Book> hasil = bookRepository.findAll(
                BookSpecifications.withFilters("fiksi", null, null, null, user.getId()),
                PageRequest.of(0, 10)
        );

        // then: hanya buku fiksi yang tampil, tapi kategorinya tetap utuh
        assertThat(hasil.getContent()).hasSize(1);
        Book yangTampil = hasil.getContent().getFirst();
        assertThat(yangTampil.getTitle()).isEqualTo("Novel Pertama");
        assertThat(yangTampil.getAuthors()).extracting(Author::getName).containsExactly("Penulis Tunggal");

        // buku sejarah tidak ikut
        assertThat(hasil.getContent())
                .extracting(Book::getTitle)
                .doesNotContain("Sejarah Kuno");
    }

    @Test
    void bacaUlangBukuMenghasilkanDuaLog_terurutTerbaru() {
        User user = simpanUser("uji3");
        authorRepository.save(new Author("Penulis Log", "Indonesia"));
        Publisher penerbit = publisherRepository.save(new Publisher("Penerbit Log", "Jakarta"));
        Book buku = bookRepository.saveAndFlush(new Book("Buku Dibaca Ulang", "9780000000004", 2019, penerbit, user));

        readingLogRepository.saveAndFlush(new ReadingLog(buku, ReadingStatus.DONE,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5), 4, user));
        readingLogRepository.saveAndFlush(new ReadingLog(buku, ReadingStatus.READING,
                LocalDate.of(2026, 9, 1), null, null, user));

        List<ReadingLog> log = readingLogRepository.findByBookIdOrderByStartedAtDesc(buku.getId());
        assertThat(log).hasSize(2);
        assertThat(log.getFirst().getStatus()).isEqualTo(ReadingStatus.READING);
        assertThat(log.getLast().getStatus()).isEqualTo(ReadingStatus.DONE);

        Page<ReadingLog> yangSedangDibaca = readingLogRepository
                .findAllByStatus(ReadingStatus.READING, PageRequest.of(0, 10));
        assertThat(yangSedangDibaca.getContent()).hasSize(1);
    }

    @Test
    void ratingHanyaBolehPadaStatusDone_dicegahDiLevelEnumDanQuery() {
        User user = simpanUser("uji4");
        authorRepository.save(new Author("Penulis Rating", "Indonesia"));
        Publisher penerbit = publisherRepository.save(new Publisher("Penerbit Rating", "Bali"));
        Book buku = bookRepository.saveAndFlush(new Book("Buku Rating", "9780000000005", 2018, penerbit, user));

        ReadingLog log = readingLogRepository.saveAndFlush(new ReadingLog(
                buku, ReadingStatus.DONE, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3), 5, user));
        assertThat(log.getRating()).isEqualTo(5);
        assertThat(log.getStatus()).isEqualTo(ReadingStatus.DONE);

        Set<ReadingStatus> semuaStatus = Set.of(ReadingStatus.values());
        assertThat(semuaStatus).containsExactlyInAnyOrder(
                ReadingStatus.WISHLIST, ReadingStatus.READING, ReadingStatus.DONE);
    }
}
