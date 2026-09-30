package dev.bookshelf.book;

import dev.bookshelf.author.AuthorResponse;
import dev.bookshelf.category.CategoryResponse;
import java.util.List;

/**
 * Versi ringkas untuk daftar buku (GET /api/books).
 * Publisher hanya dikirim id + nama; penulis dan kategori tetap utuh.
 */
public record BookSummaryResponse(
        Long id,
        String title,
        String isbn,
        Integer publishYear,
        String publisherName,
        List<AuthorResponse> authors,
        List<CategoryResponse> categories
) {
    public static BookSummaryResponse of(Book b) {
        return new BookSummaryResponse(
                b.getId(),
                b.getTitle(),
                b.getIsbn(),
                b.getPublishYear(),
                b.getPublisher() == null ? null : b.getPublisher().getName(),
                b.getAuthors().stream().map(AuthorResponse::of).sorted(java.util.Comparator.comparing(AuthorResponse::name)).toList(),
                b.getCategories().stream().map(CategoryResponse::of).sorted(java.util.Comparator.comparing(CategoryResponse::name)).toList()
        );
    }
}
