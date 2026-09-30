package dev.bookshelf.publisher;

/** PATCH /api/publishers/{id} — semua field opsional. */
public record PublisherPatchRequest(
        String name,
        String city
) {
    public boolean hasName() {
        return name != null;
    }

    public boolean hasCity() {
        return city != null;
    }

    /** Validasi manual: nama yang dikirim tidak boleh kosong. */
    public void validate() {
        if (hasName() && name.isBlank()) {
            throw new IllegalArgumentException("Nama penerbit tidak boleh kosong");
        }
        if (hasName() && name.trim().length() > 150) {
            throw new IllegalArgumentException("Nama penerbit maksimal 150 karakter");
        }
        if (hasCity() && city.length() > 80) {
            throw new IllegalArgumentException("Kota maksimal 80 karakter");
        }
    }
}
