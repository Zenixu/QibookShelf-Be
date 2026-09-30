package dev.bookshelf.book;

import dev.bookshelf.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public PageResponse<BookSummaryResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long author,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer year,
            Pageable pageable
    ) {
        return PageResponse.of(bookService.list(category, author, q, year, pageable));
    }

    @GetMapping("/{id}")
    public BookDetailResponse getById(@PathVariable Long id) {
        return bookService.getDetailById(id);
    }

    @PostMapping
    public ResponseEntity<BookDetailResponse> create(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @PutMapping("/{id}")
    public BookDetailResponse update(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request
    ) {
        return bookService.update(id, request);
    }

    @PatchMapping("/{id}")
    public BookDetailResponse patch(
            @PathVariable Long id,
            @Valid @RequestBody BookPatchRequest request
    ) {
        return bookService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }
}
