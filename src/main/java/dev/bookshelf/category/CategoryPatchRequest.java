package dev.bookshelf.category;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * PATCH /api/categories/{id} — semua field opsional;
 * hanya field yang dikirim (dan tidak kosong) yang diubah.
 * Slug tidak diturunkan dari nama saat PATCH: slug lama dipertahankan
 * kecuali dikirim ulang secara eksplisit.
 */
public record CategoryPatchRequest(
        @Size(max = 80, message = "Nama kategori maksimal 80 karakter")
        String name,

        @Size(max = 80, message = "Slug maksimal 80 karakter")
        @Pattern(regexp = "^[a-z0-9-]*$", message = "Slug hanya boleh huruf kecil, angka, dan tanda hubung")
        String slug
) {
    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public boolean hasSlug() {
        return slug != null && !slug.isBlank();
    }
}
