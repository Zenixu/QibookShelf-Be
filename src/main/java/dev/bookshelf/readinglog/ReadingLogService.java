package dev.bookshelf.readinglog;

import dev.bookshelf.book.Book;
import dev.bookshelf.book.BookRepository;
import dev.bookshelf.common.exception.ConflictException;
import dev.bookshelf.common.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class ReadingLogService {

    private final ReadingLogRepository readingLogRepository;
    private final BookRepository bookRepository;

    public ReadingLogService(ReadingLogRepository readingLogRepository, BookRepository bookRepository) {
        this.readingLogRepository = readingLogRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * GET /api/books/{bookId}/reading-logs — riwayat baca satu buku (paling baru duluan).
     */
    public java.util.List<ReadingLogResponse> listByBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new NotFoundException("Buku id %d tidak ditemukan".formatted(bookId));
        }
        return readingLogRepository.findByBookIdOrderByStartedAtDesc(bookId).stream()
                .map(ReadingLogResponse::of)
                .toList();
    }

    /**
     * GET /api/reading-logs?status= — semua log dengan filter status opsional.
     */
    public Page<ReadingLogResponse> listByStatus(ReadingStatus status, Pageable pageable) {
        Page<ReadingLog> page = (status == null)
                ? readingLogRepository.findAll(pageable)
                : readingLogRepository.findAllByStatus(status, pageable);
        return page.map(ReadingLogResponse::of);
    }

    @Transactional
    public ReadingLogResponse create(Long bookId, ReadingLogRequest request) {
        Book book = cariBuku(bookId);
        validasiAturanBisnis(request, null);
        ReadingLog log = new ReadingLog(
                book, request.status(), request.startedAt(), request.finishedAt(), request.rating());
        return ReadingLogResponse.of(readingLogRepository.saveAndFlush(log));
    }

    @Transactional
    public ReadingLogResponse update(Long bookId, Long logId, ReadingLogRequest request) {
        cariBuku(bookId);
        ReadingLog log = readingLogRepository.findById(logId)
                .filter(l -> l.getBook().getId().equals(bookId))
                .orElseThrow(() -> new NotFoundException(
                        "Reading log id %d untuk buku %d tidak ditemukan".formatted(logId, bookId)));
        validasiAturanBisnis(request, log);
        log.setStatus(request.status());
        log.setStartedAt(request.startedAt());
        log.setFinishedAt(request.finishedAt());
        log.setRating(request.rating());
        return ReadingLogResponse.of(log);
    }

    @Transactional
    public void delete(Long bookId, Long logId) {
        cariBuku(bookId);
        ReadingLog log = readingLogRepository.findById(logId)
                .filter(l -> l.getBook().getId().equals(bookId))
                .orElseThrow(() -> new NotFoundException(
                        "Reading log id %d untuk buku %d tidak ditemukan".formatted(logId, bookId)));
        readingLogRepository.delete(log);
    }

    // ===== aturan bisnis (lihat PRD.md) =====

    private void validasiAturanBisnis(ReadingLogRequest request, ReadingLog existing) {
        // rating hanya boleh diisi saat DONE
        if (request.hasRating() && request.status() != ReadingStatus.DONE) {
            throw new ConflictException("Rating hanya boleh diisi pada status DONE");
        }

        // DONE wajib ada finished_at
        if (request.status() == ReadingStatus.DONE && request.finishedAt() == null) {
            throw new ConflictException("Status DONE wajib mengisi finishedAt");
        }

        // finished_at hanya boleh diisi saat DONE
        if (request.finishedAt() != null && request.status() != ReadingStatus.DONE) {
            throw new ConflictException("finishedAt hanya boleh diisi pada status DONE");
        }

        // started_at wajib saat READING atau DONE
        if (request.status() != ReadingStatus.WISHLIST && request.startedAt() == null) {
            throw new ConflictException("Status %s wajib mengisi startedAt".formatted(request.status()));
        }

        // finished_at tidak boleh sebelum started_at
        if (request.finishedAt() != null && request.startedAt() != null
                && request.finishedAt().isBefore(request.startedAt())) {
            throw new ConflictException("finishedAt tidak boleh sebelum startedAt");
        }

        // tanggal tidak boleh di masa depan
        LocalDate hariIni = LocalDate.now();
        if (request.startedAt() != null && request.startedAt().isAfter(hariIni)) {
            throw new ConflictException("startedAt tidak boleh di masa depan");
        }
        if (request.finishedAt() != null && request.finishedAt().isAfter(hariIni)) {
            throw new ConflictException("finishedAt tidak boleh di masa depan");
        }
    }

    private Book cariBuku(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Buku id %d tidak ditemukan".formatted(bookId)));
    }
}
