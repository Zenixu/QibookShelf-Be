package dev.bookshelf.author;

import java.util.List;

/** PATCH /api/authors/{id} — semua field opsional. */
public record AuthorPatchRequest(
        String name,
        String nationality
) {
    public boolean hasName() {
        return name != null;
    }

    public boolean hasNationality() {
        return nationality != null;
    }

    /** Validasi manual: nama yang dikirim tidak boleh kosong. */
    public void validate() {
        if (hasName() && name.isBlank()) {
            throw new IllegalArgumentException("Nama penulis tidak boleh kosong");
        }
        if (hasName() && name.trim().length() > 150) {
            throw new IllegalArgumentException("Nama penulis maksimal 150 karakter");
        }
        if (hasNationality() && nationality.length() > 80) {
            throw new IllegalArgumentException("Kebangsaan maksimal 80 karakter");
        }
    }

    public List<String> presentFields() {
        return java.util.stream.Stream.of(
                hasName() ? "name" : null,
                hasNationality() ? "nationality" : null)
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
