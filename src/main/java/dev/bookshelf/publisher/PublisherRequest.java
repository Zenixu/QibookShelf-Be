package dev.bookshelf.publisher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublisherRequest(
        @NotBlank(message = "Nama penerbit wajib diisi")
        @Size(max = 150, message = "Nama penerbit maksimal 150 karakter")
        String name,

        @Size(max = 80, message = "Kota maksimal 80 karakter")
        String city
) {
}
