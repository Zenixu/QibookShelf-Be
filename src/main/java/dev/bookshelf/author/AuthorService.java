package dev.bookshelf.author;

import dev.bookshelf.common.exception.ConflictException;
import dev.bookshelf.common.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public Page<AuthorResponse> list(String q, Pageable pageable) {
        Page<Author> page = StringUtils.hasText(q)
                ? authorRepository.searchByName(q, pageable)
                : authorRepository.findAll(pageable);
        return page.map(AuthorResponse::of);
    }

    public AuthorResponse getById(Long id) {
        return AuthorResponse.of(findAuthorOrThrow(id));
    }

    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        if (authorRepository.existsByName(request.name())) {
            throw new ConflictException("Penulis '%s' sudah terdaftar".formatted(request.name()));
        }
        Author author = new Author(request.name(), request.nationality());
        return AuthorResponse.of(authorRepository.save(author));
    }

    @Transactional
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = findAuthorOrThrow(id);
        if (!author.getName().equals(request.name())
                && authorRepository.existsByName(request.name())) {
            throw new ConflictException("Penulis '%s' sudah terdaftar".formatted(request.name()));
        }
        author.setName(request.name());
        author.setNationality(request.nationality());
        return AuthorResponse.of(author);
    }

    /**
     * PATCH /api/authors/{id} — hanya field yang dikirim yang berubah.
     */
    @Transactional
    public AuthorResponse patch(Long id, AuthorPatchRequest request) {
        Author author = findAuthorOrThrow(id);
        if (request.hasName() && !author.getName().equals(request.name())) {
            if (authorRepository.existsByName(request.name())) {
                throw new ConflictException("Penulis '%s' sudah terdaftar".formatted(request.name()));
            }
            author.setName(request.name());
        }
        if (request.hasNationality()) {
            author.setNationality(request.nationality());
        }
        return AuthorResponse.of(author);
    }

    @Transactional
    public void delete(Long id) {
        Author author = findAuthorOrThrow(id);
        if (authorRepository.isAuthorUsedByBooks(id)) {
            throw new ConflictException(
                    "Penulis '%s' masih dipakai oleh buku; hapus bukunya terlebih dahulu"
                            .formatted(author.getName()));
        }
        authorRepository.delete(author);
    }

    Author findAuthorOrThrow(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Penulis id %d tidak ditemukan".formatted(id)));
    }
}
