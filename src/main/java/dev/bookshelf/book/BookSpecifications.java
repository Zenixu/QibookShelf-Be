package dev.bookshelf.book;

import dev.bookshelf.author.Author;
import dev.bookshelf.category.Category;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kombinasi filter dinamis untuk GET /api/books.
 * Filter hanya menyaring buku mana yang tampil, tetapi setiap buku tetap
 * menampilkan seluruh penulis dan kategorinya (lihat API-CONTRACT.md).
 */
public final class BookSpecifications {

    private BookSpecifications() {
    }

    public static Specification<Book> withFilters(
            String categorySlug,
            Long authorId,
            String q,
            Integer year,
            Long userId
    ) {
        // Specification.and(null) ditolak di Spring Data JPA terbaru → dirakit kondisional
        // Hindari Specification.where(null) yang ambigu di Spring Data JPA terbaru
        Specification<Book> spec = (root, query, cb) -> cb.conjunction();
        
        // MULTI-TENANCY: Filter berdasarkan user_id (WAJIB)
        if (userId != null) {
            spec = spec.and(belongsToUser(userId));
        }
        
        if (categorySlug != null && !categorySlug.isBlank()) {
            spec = spec.and(hasCategorySlug(categorySlug));
        }
        if (authorId != null) {
            spec = spec.and(hasAuthorId(authorId));
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and(hasTitleLike(q));
        }
        if (year != null) {
            spec = spec.and(hasPublishYear(year));
        }
        return spec;
    }

    static Specification<Book> belongsToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    static Specification<Book> hasCategorySlug(String categorySlug) {
        if (categorySlug == null || categorySlug.isBlank()) {
            return null;
        }
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Book, Category> categories = root.join("categories");
            return cb.equal(cb.lower(categories.get("slug")), categorySlug.toLowerCase());
        };
    }

    static Specification<Book> hasAuthorId(Long authorId) {
        if (authorId == null) {
            return null;
        }
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Book, Author> authors = root.join("authors");
            return cb.equal(authors.get("id"), authorId);
        };
    }

    static Specification<Book> hasTitleLike(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%");
    }

    static Specification<Book> hasPublishYear(Integer year) {
        if (year == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("publishYear"), year);
    }

    static Set<Long> idsOrEmpty(Set<Long> ids) {
        return ids == null ? Set.of() : ids.stream().collect(Collectors.toUnmodifiableSet());
    }
}
