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
     * MULTI-TENANCY: Hanya menampilkan log milik user yang sedang login.
     */
    public java.util.List<ReadingLogResponse> listByBook(Long bookId) {
        Long currentUserId = dev.bookshelf.common.SecurityUtil.getCurrentUserId();
        bookRepository.findById(bookId)
                .filter(b -> b.getUser() != null && b.getUser().getId().equals(currentUserId))
                .orElseThrow(() -> new NotFoundException("Buku id %d tidak ditemukan".formatted(bookId)));

        return readingLogRepository.findByBookIdAndUserIdOrderByStartedAtDesc(bookId, currentUserId).stream()
                .map(ReadingLogResponse::of)
                .toList();
    }

    /**
     * GET /api/reading-logs?status= — semua log milik user (lihat API-CONTRACT.md).
     * MULTI-TENANCY: Hanya menampilkan log milik user yang sedang login.
     */
    public Page<ReadingLogResponse> listByStatus(ReadingStatus status, Pageable pageable) {
        Long currentUserId = dev.bookshelf.common.SecurityUtil.getCurrentUserId();
        Page<ReadingLog> page = (status == null)
                ? readingLogRepository.findByUserId(currentUserId, pageable)
                : readingLogRepository.findByUserIdAndStatus(currentUserId, status, pageable);

        return page.map(ReadingLogResponse::of);
    }

    /**
     * GET /api/reading-logs/{id} — detail satu log milik user.
     */
    public ReadingLogResponse getById(Long logId) {
        return ReadingLogResponse.of(findLogOrThrow(logId));
    }

    @Transactional
    public ReadingLogResponse create(Long bookId, ReadingLogRequest request) {
        dev.bookshelf.security.User currentUser = dev.bookshelf.common.SecurityUtil.getCurrentUser();
        Long currentUserId = currentUser.getId();

        Book book = bookRepository.findById(bookId)
                .filter(b -> b.getUser() != null && b.getUser().getId().equals(currentUserId))
                .orElseThrow(() -> new NotFoundException("Buku id %d tidak ditemukan".formatted(bookId)));

        validasiAturanBisnis(request.status(), request.startedAt(), request.finishedAt(), request.rating());
        ReadingLog log = new ReadingLog(
                book, request.status(), request.startedAt(), request.finishedAt(), request.rating(), currentUser);
        return ReadingLogResponse.of(readingLogRepository.saveAndFlush(log));
    }

    /**
     * PUT /api/books/{bookId}/reading-logs/{logId} — ganti seluruh isi log
     * (kontrak: validasi penuh seperti POST).
     */
    @Transactional
    public ReadingLogResponse update(Long bookId, Long logId, ReadingLogRequest request) {
        Long currentUserId = dev.bookshelf.common.SecurityUtil.getCurrentUserId();

        bookRepository.findById(bookId)
                .filter(b -> b.getUser() != null && b.getUser().getId().equals(currentUserId))
                .orElseThrow(() -> new NotFoundException("Buku id %d tidak ditemukan".formatted(bookId)));

        ReadingLog log = readingLogRepository.findById(logId)
                .filter(l -> l.getBook().getId().equals(bookId))
                .filter(l -> l.getUser() != null && l.getUser().getId().equals(currentUserId))
                .orElseThrow(() -> new NotFoundException(
                        "Reading log id %d untuk buku %d tidak ditemukan".formatted(logId, bookId)));

        validasiAturanBisnis(request.status(), request.startedAt(), request.finishedAt(), request.rating());
        log.setStatus(request.status());
        log.setStartedAt(request.startedAt());
        log.setFinishedAt(request.finishedAt());
        log.setRating(request.rating());
        return ReadingLogResponse.of(log);
    }

    /**
     * PATCH /api/reading-logs/{id} — hanya field yang dikirim yang diubah;
     * aturan bisnis divalidasi terhadap KEADAAN AKHIR log (lihat API-CONTRACT.md).
     */
    @Transactional
    public ReadingLogResponse patch(Long logId, ReadingLogPatchRequest request) {
        ReadingLog log = findLogOrThrow(logId);

        ReadingStatus statusBaru = request.hasStatus() ? request.status() : log.getStatus();
        LocalDate mulaiBaru = request.hasStartedAt() ? request.startedAt() : log.getStartedAt();
        LocalDate selesaiBaru = request.hasFinishedAt() ? request.finishedAt() : log.getFinishedAt();
        Integer ratingBaru = request.hasRating() ? request.rating() : log.getRating();

        validasiAturanBisnis(statusBaru, mulaiBaru, selesaiBaru, ratingBaru);
        log.setStatus(statusBaru);
        log.setStartedAt(mulaiBaru);
        log.setFinishedAt(selesaiBaru);
        log.setRating(ratingBaru);
        return ReadingLogResponse.of(log);
    }

    /**
     * DELETE /api/reading-logs/{id} — hapus log milik user (lihat API-CONTRACT.md).
     */
    @Transactional
    public void deleteById(Long logId) {
        readingLogRepository.delete(findLogOrThrow(logId));
    }

    @Transactional
    public void delete(Long bookId, Long logId) {
        Long currentUserId = dev.bookshelf.common.SecurityUtil.getCurrentUserId();
        
        Book book = bookRepository.findById(bookId)
                .filter(b -> b.getUser() != null && b.getUser().getId().equals(currentUserId))
                .orElseThrow(() -> new NotFoundException("Buku id %d tidak ditemukan".formatted(bookId)));
        
        ReadingLog log = readingLogRepository.findById(logId)
                .filter(l -> l.getBook().getId().equals(bookId))
                .filter(l -> l.getUser() != null && l.getUser().getId().equals(currentUserId))
                .orElseThrow(() -> new NotFoundException(
                        "Reading log id %d untuk buku %d tidak ditemukan".formatted(logId, bookId)));
        
        readingLogRepository.delete(log);
    }

    // ===== aturan bisnis (lihat PRD.md / API-CONTRACT.md) =====

    private void validasiAturanBisnis(ReadingStatus status, LocalDate mulai, LocalDate selesai, Integer rating) {
        // rating hanya boleh diisi saat DONE (1-5 dicek via @Min/@Max pada POST/PUT)
        if (rating != null) {
            if (status != ReadingStatus.DONE) {
                throw new ConflictException("Rating hanya boleh diisi pada status DONE");
            }
            if (rating < 1 || rating > 5) {
                throw new ConflictException("Rating harus antara 1 dan 5");
            }
        }

        // DONE wajib ada finished_at
        if (status == ReadingStatus.DONE && selesai == null) {
            throw new ConflictException("Status DONE wajib mengisi finishedAt");
        }

        // finished_at hanya boleh diisi saat DONE
        if (selesai != null && status != ReadingStatus.DONE) {
            throw new ConflictException("finishedAt hanya boleh diisi pada status DONE");
        }

        // started_at wajib saat READING atau DONE
        if (status != ReadingStatus.WISHLIST && mulai == null) {
            throw new ConflictException("Status %s wajib mengisi startedAt".formatted(status));
        }

        // finished_at tidak boleh sebelum started_at
        if (selesai != null && mulai != null
                && selesai.isBefore(mulai)) {
            throw new ConflictException("finishedAt tidak boleh sebelum startedAt");
        }

        // tanggal tidak boleh di masa depan
        LocalDate hariIni = LocalDate.now();
        if (mulai != null && mulai.isAfter(hariIni)) {
            throw new ConflictException("startedAt tidak boleh di masa depan");
        }
        if (selesai != null && selesai.isAfter(hariIni)) {
            throw new ConflictException("finishedAt tidak boleh di masa depan");
        }
    }

    private ReadingLog findLogOrThrow(Long logId) {
        Long currentUserId = dev.bookshelf.common.SecurityUtil.getCurrentUserId();
        return readingLogRepository.findByIdAndUserId(logId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Reading log id %d tidak ditemukan".formatted(logId)));
    }

    private Book cariBuku(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Buku id %d tidak ditemukan".formatted(bookId)));
    }
}
