package dev.bookshelf.readinglog;

import dev.bookshelf.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books/{bookId}/reading-logs")
public class ReadingLogController {

    private final ReadingLogService readingLogService;

    public ReadingLogController(ReadingLogService readingLogService) {
        this.readingLogService = readingLogService;
    }

    @GetMapping
    public List<ReadingLogResponse> listByBook(@PathVariable Long bookId) {
        return readingLogService.listByBook(bookId);
    }

    @PostMapping
    public ResponseEntity<ReadingLogResponse> create(
            @PathVariable Long bookId,
            @Valid @RequestBody ReadingLogRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(readingLogService.create(bookId, request));
    }

    @PutMapping("/{logId}")
    public ReadingLogResponse update(
            @PathVariable Long bookId,
            @PathVariable Long logId,
            @Valid @RequestBody ReadingLogRequest request
    ) {
        return readingLogService.update(bookId, logId, request);
    }

    @DeleteMapping("/{logId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long bookId, @PathVariable Long logId) {
        readingLogService.delete(bookId, logId);
    }
}
