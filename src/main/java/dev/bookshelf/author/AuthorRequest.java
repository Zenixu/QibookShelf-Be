package dev.bookshelf.author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(
        @NotBlank(message = "Nama penulis wajib diisi")
        @Size(max = 150, message = "Nama penulis maksimal 150 karakter")
        String name,

        @Size(max = 80, message = "Kebangsaan maksimal 80 karakter")
        String nationality
) {
}
