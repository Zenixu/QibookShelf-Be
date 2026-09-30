package dev.bookshelf.author;

import jakarta.validation.constraints.Size;

/**
 * PATCH /api/authors/{id} — semua field opsional;
 * hanya field yang dikirim (dan tidak kosong) yang diubah.
 */
public record AuthorPatchRequest(
        @Size(max = 150, message = "Nama penulis maksimal 150 karakter")
        String name,

        @Size(max = 80, message = "Kebangsaan maksimal 80 karakter")
        String nationality
) {
    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public boolean hasNationality() {
        return nationality != null && !nationality.isBlank();
    }
}
