package dev.bookshelf.readinglog;

import dev.bookshelf.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reading-logs")
public class ReadingLogGlobalController {

    private final ReadingLogService readingLogService;

    public ReadingLogGlobalController(ReadingLogService readingLogService) {
        this.readingLogService = readingLogService;
    }

    /**
     * GET /api/reading-logs?status= — seluruh log milik user yang sedang login.
     * Status opsional: WISHLIST | READING | DONE. Nilai lain → 400.
     */
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
            @Valid @RequestBody ReadingLogRequest request
    ) {
        return readingLogService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        readingLogService.deleteById(id);
    }
}
