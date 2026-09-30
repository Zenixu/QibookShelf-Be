package dev.bookshelf.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Nama kategori wajib diisi")
        @Size(max = 80, message = "Nama kategori maksimal 80 karakter")
        String name,

        @Size(max = 80, message = "Slug maksimal 80 karakter")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug harus cocok ^[a-z0-9]+(-[a-z0-9]+)*$")
        String slug
) {
}
