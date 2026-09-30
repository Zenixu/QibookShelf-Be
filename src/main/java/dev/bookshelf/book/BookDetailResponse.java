package dev.bookshelf.book;

import dev.bookshelf.author.AuthorResponse;
import dev.bookshelf.category.CategoryResponse;
import dev.bookshelf.publisher.PublisherResponse;
import java.util.List;

public record BookDetailResponse(
        Long id,
        String title,
        String isbn,
        Integer publishYear,
        PublisherResponse publisher,
        List<AuthorResponse> authors,
        List<CategoryResponse> categories
) {
    public static BookDetailResponse of(Book b) {
        return new BookDetailResponse(
                b.getId(),
                b.getTitle(),
                b.getIsbn(),
                b.getPublishYear(),
                PublisherResponse.of(b.getPublisher()),
                b.getAuthors().stream().map(AuthorResponse::of).sorted(java.util.Comparator.comparing(AuthorResponse::name)).toList(),
                b.getCategories().stream().map(CategoryResponse::of).sorted(java.util.Comparator.comparing(CategoryResponse::name)).toList()
        );
    }
}
