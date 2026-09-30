package dev.bookshelf.readinglog;

import java.time.LocalDate;

/**
 * PATCH /api/reading-logs/{id} — semua field opsional; hanya field yang
 * dikirim yang diubah. Aturan bisnis divalidasi terhadap keadaan AKHIR
 * log (gabungan data lama + field baru), bukan hanya field yang dikirim.
 */
public record ReadingLogPatchRequest(
        ReadingStatus status,
        LocalDate startedAt,
        LocalDate finishedAt,
        Integer rating
) {
    public boolean hasStatus() {
        return status != null;
    }

    public boolean hasStartedAt() {
        return startedAt != null;
    }

    public boolean hasFinishedAt() {
        return finishedAt != null;
    }

    public boolean hasRating() {
        return rating != null;
    }
}
