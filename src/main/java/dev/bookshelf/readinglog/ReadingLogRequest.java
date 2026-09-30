package dev.bookshelf.readinglog;

import dev.bookshelf.book.BookDetailResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReadingLogRequest(
        @NotNull(message = "Status wajib diisi")
        ReadingStatus status,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startedAt,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate finishedAt,

        @Min(value = 1, message = "Rating minimal 1")
        @Max(value = 5, message = "Rating maksimal 5")
        Integer rating
) {
    public boolean hasRating() {
        return rating != null;
    }
}
