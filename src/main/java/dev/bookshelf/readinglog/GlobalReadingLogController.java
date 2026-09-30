package dev.bookshelf.readinglog;

import dev.bookshelf.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoint global sesuai API-CONTRACT.md:
 * GET /api/reading-logs, GET /api/reading-logs/{id},
 * PATCH /api/reading-logs/{id}, DELETE /api/reading-logs/{id}.
 */
@RestController
@RequestMapping("/api/reading-logs")
public class GlobalReadingLogController {

    private final ReadingLogService readingLogService;

    public GlobalReadingLogController(ReadingLogService readingLogService) {
        this.readingLogService = readingLogService;
    }

    @GetMapping
    public PageResponse<ReadingLogResponse> list(
            @RequestParam(required = false) ReadingStatus status,
            Pageable pageable
    ) {
        return PageResponse.of(readingLogService.listByStatus(status, pageable));
    }

    @GetMapping("/{id}")
    public ReadingLogResponse getById(@PathVariable Long id) {
        return readingLogService.getById(id);
    }

    @PatchMapping("/{id}")
    public ReadingLogResponse patch(
            @PathVariable Long id,
            @RequestBody ReadingLogPatchRequest request
    ) {
        return readingLogService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        readingLogService.deleteById(id);
    }

    @PutMapping("/{id}")
    public ReadingLogResponse replace(
            @PathVariable Long id,
            @Valid @RequestBody GlobalReadingLogPutRequest request
    ) {
        return readingLogService.patch(id, new ReadingLogPatchRequest(
                request.status(), request.startedAt(), request.finishedAt(), request.rating()));
    }

    /** PUT butuh status wajib (ganti penuh); dipakai ulang lewat record PATCH. */
    public record GlobalReadingLogPutRequest(
            @jakarta.validation.constraints.NotNull(message = "Status wajib diisi") ReadingStatus status,
            java.time.LocalDate startedAt,
            java.time.LocalDate finishedAt,
            @jakarta.validation.constraints.Min(value = 1, message = "Rating minimal 1")
            @jakarta.validation.constraints.Max(value = 5, message = "Rating maksimal 5") Integer rating
    ) {
    }
}
