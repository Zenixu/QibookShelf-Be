package dev.bookshelf.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BookRequest(
        @NotBlank(message = "Judul wajib diisi")
        @Size(max = 255, message = "Judul maksimal 255 karakter")
        String title,

        @Size(max = 13, message = "ISBN maksimal 13 karakter")
        @Pattern(regexp = "^$|^[0-9]{10,13}$", message = "ISBN harus 10-13 digit angka")
        String isbn,

        Integer publishYear,

        @NotNull(message = "Penerbit wajib diisi")
        Long publisherId,

        List<Long> authorIds,

        List<Long> categoryIds
) {
    public List<Long> safeAuthorIds() {
        return authorIds == null ? List.of() : authorIds;
    }

    public List<Long> safeCategoryIds() {
        return categoryIds == null ? List.of() : categoryIds;
    }
}
