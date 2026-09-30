package dev.bookshelf.book;

import dev.bookshelf.author.Author;
import dev.bookshelf.author.AuthorRepository;
import dev.bookshelf.category.Category;
import dev.bookshelf.category.CategoryRepository;
import dev.bookshelf.common.exception.ConflictException;
import dev.bookshelf.common.exception.NotFoundException;
import dev.bookshelf.publisher.Publisher;
import dev.bookshelf.publisher.PublisherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       PublisherRepository publisherRepository,
                       CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * GET /api/books?category=&author=&q=&year=
     * Filter hanya menyaring buku yang tampil; tiap buku tetap menampilkan
     * seluruh penulis & kategorinya (lihat API-CONTRACT.md).
     * Fetch join dilakukan per-halaman untuk menghindari N+1.
     * 
     * MULTI-TENANCY: Hanya menampilkan buku milik user yang sedang login.
     */
    public Page<BookSummaryResponse> list(String category, Long author, String q, Integer year, Pageable pageable) {
        Long currentUserId = dev.bookshelf.common.SecurityUtil.getCurrentUserId();
        Page<Book> page = bookRepository.findAll(
                BookSpecifications.withFilters(category, author, q, year, currentUserId), pageable);
        return page.map(this::toSummary);
    }

    public BookDetailResponse getDetailById(Long id) {
        Book book = bookRepository.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Buku id %d tidak ditemukan".formatted(id)));
        return BookDetailResponse.of(book);
    }

    @Transactional
    public BookDetailResponse create(BookRequest request) {
        dev.bookshelf.security.User currentUser = dev.bookshelf.common.SecurityUtil.getCurrentUser();
        
        if (bookRepository.findByIsbn(request.isbn()).isPresent()) {
            throw new ConflictException("ISBN '%s' sudah terdaftar".formatted(request.isbn()));
        }
        Publisher publisher = findPublisherOrThrow(request.publisherId());
        Book book = new Book(request.title(), request.isbn(), request.publishYear(), publisher, currentUser);
        gantiPenulis(book, request.safeAuthorIds());
        gantiKategori(book, request.safeCategoryIds());
        return BookDetailResponse.of(bookRepository.saveAndFlush(book));
    }

    @Transactional
    public BookDetailResponse update(Long id, BookRequest request) {
        Book book = findBookOrThrow(id);
        bookRepository.findByIsbn(request.isbn())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("ISBN '%s' sudah terdaftar".formatted(request.isbn()));
                });
        book.setTitle(request.title());
        book.setIsbn(request.isbn());
        book.setPublishYear(request.publishYear());
        if (request.publisherId() != null) {
            book.setPublisher(findPublisherOrThrow(request.publisherId()));
        }
        gantiPenulis(book, request.safeAuthorIds());
        gantiKategori(book, request.safeCategoryIds());
        return BookDetailResponse.of(book);
    }

    /**
     * PATCH /api/books/{id} — pembaruan parsial; hanya field yang dikirim yang diubah.
     */
    @Transactional
    public BookDetailResponse patch(Long id, BookPatchRequest request) {
        Book book = findBookOrThrow(id);

        if (request.hasTitle()) {
            book.setTitle(request.title());
        }
        if (request.hasIsbn()) {
            bookRepository.findByIsbn(request.isbn())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ConflictException("ISBN '%s' sudah terdaftar".formatted(request.isbn()));
                    });
            book.setIsbn(request.isbn());
        }
        if (request.hasPublishYear()) {
            book.setPublishYear(request.publishYear());
        }
        if (request.hasPublisher()) {
            book.setPublisher(findPublisherOrThrow(request.publisherId()));
        }
        if (request.hasAuthors()) {
            gantiPenulis(book, request.authorIds());
        }
        if (request.hasCategories()) {
            gantiKategori(book, request.categoryIds());
        }
        return BookDetailResponse.of(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = findBookOrThrow(id);
        // reading_logs ikut terhapus via ON DELETE CASCADE di V1__init.sql
        bookRepository.delete(book);
    }

    // ===== helper =====

    private void gantiPenulis(Book book, List<Long> authorIds) {
        Set<Author> authors = resolveByIds(authorIds, authorRepository::findById, "Penulis");
        book.getAuthors().clear();
        book.getAuthors().addAll(authors);
    }

    private void gantiKategori(Book book, List<Long> categoryIds) {
        Set<Category> categories = resolveByIds(categoryIds, categoryRepository::findById, "Kategori");
        book.getCategories().clear();
        book.getCategories().addAll(categories);
    }

    private <T> Set<T> resolveByIds(List<Long> ids, java.util.function.Function<Long, java.util.Optional<T>> finder, String label) {
        if (ids.isEmpty()) {
            return new HashSet<>();
        }
        Set<T> hasil = new HashSet<>();
        for (Long idBaru : ids) {
            T entitas = finder.apply(idBaru)
                    .orElseThrow(() -> new NotFoundException("%s id %d tidak ditemukan".formatted(label, idBaru)));
            hasil.add(entitas);
        }
        return hasil;
    }

    private BookSummaryResponse toSummary(Book b) {
        // dipakai untuk list; relasi ringan sudah dimuat via entity graph di repository
        return BookSummaryResponse.of(b);
    }

    private Book findBookOrThrow(Long id) {
        Long currentUserId = dev.bookshelf.common.SecurityUtil.getCurrentUserId();
        return bookRepository.findWithDetailsById(id)
                .filter(book -> book.getUser() != null && book.getUser().getId().equals(currentUserId))
                .orElseThrow(() -> new NotFoundException("Buku id %d tidak ditemukan".formatted(id)));
    }

    private Publisher findPublisherOrThrow(Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Penerbit id %d tidak ditemukan".formatted(id)));
    }
}
