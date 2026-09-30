package dev.bookshelf.readinglog;

import dev.bookshelf.book.Book;

import java.time.LocalDate;

public record ReadingLogResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String status,
        LocalDate startedAt,
        LocalDate finishedAt,
        Integer rating
) {
    public static ReadingLogResponse of(ReadingLog log) {
        Book buku = log.getBook();
        return new ReadingLogResponse(
                log.getId(),
                buku == null ? null : buku.getId(),
                buku == null ? null : buku.getTitle(),
                log.getStatus().name(),
                log.getStartedAt(),
                log.getFinishedAt(),
                log.getRating()
        );
    }
}
