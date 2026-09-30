package dev.bookshelf.publisher;

import jakarta.validation.constraints.Size;

/**
 * PATCH /api/publishers/{id} — semua field opsional;
 * hanya field yang dikirim (dan tidak kosong) yang diubah.
 */
public record PublisherPatchRequest(
        @Size(max = 150, message = "Nama penerbit maksimal 150 karakter")
        String name,

        @Size(max = 100, message = "Kota maksimal 100 karakter")
        String city
) {
    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public boolean hasCity() {
        return city != null && !city.isBlank();
    }
}
