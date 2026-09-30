package dev.bookshelf.category;

/** PATCH /api/categories/{id} — semua field opsional. */
public record CategoryPatchRequest(
        String name,
        String slug
) {
    public boolean hasName() {
        return name != null;
    }

    public boolean hasSlug() {
        return slug != null;
    }

    /** Validasi manual: mengikuti aturan CategoryRequest + CHECK constraint DB. */
    public void validate() {
        if (hasName() && name.isBlank()) {
            throw new IllegalArgumentException("Nama kategori tidak boleh kosong");
        }
        if (hasName() && name.trim().length() > 80) {
            throw new IllegalArgumentException("Nama kategori maksimal 80 karakter");
        }
        if (hasSlug() && !slug.matches("^[a-z0-9]+(-[a-z0-9]+)*$")) {
            throw new IllegalArgumentException("Slug harus cocok ^[a-z0-9]+(-[a-z0-9]+)*$");
        }
    }
}
