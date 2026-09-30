package dev.bookshelf.publisher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublisherRequest(
        @NotBlank(message = "Nama penerbit wajib diisi")
        @Size(max = 150, message = "Nama penerbit maksimal 150 karakter")
        String name,

        @Size(max = 100, message = "Kota maksimal 100 karakter")
        String city
) {
}
