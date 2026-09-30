package dev.bookshelf.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookPatchRequest(
        @Size(max = 255, message = "Judul maksimal 255 karakter")
        String title,

        @Size(max = 13, message = "ISBN maksimal 13 karakter")
        String isbn,

        Integer publishYear,

        Long publisherId,

        java.util.List<Long> authorIds,

        java.util.List<Long> categoryIds
) {
    public boolean hasTitle() {
        return title != null && !title.isBlank();
    }

    public boolean hasIsbn() {
        return isbn != null && !isbn.isBlank();
    }

    public boolean hasPublisher() {
        return publisherId != null;
    }

    public boolean hasAuthors() {
        return authorIds != null;
    }

    public boolean hasCategories() {
        return categoryIds != null;
    }

    public boolean hasPublishYear() {
        return publishYear != null;
    }
}
