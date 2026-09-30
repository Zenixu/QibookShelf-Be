package dev.bookshelf.publisher;

import dev.bookshelf.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @GetMapping
    public PageResponse<PublisherResponse> list(
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return PageResponse.of(publisherService.list(q, pageable));
    }

    @GetMapping("/{id}")
    public PublisherResponse getById(@PathVariable Long id) {
        return publisherService.getById(id);
    }

    @PostMapping
    public ResponseEntity<PublisherResponse> create(@Valid @RequestBody PublisherRequest request) {
        PublisherResponse created = publisherService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public PublisherResponse update(
            @PathVariable Long id,
            @Valid @RequestBody PublisherRequest request
    ) {
        return publisherService.update(id, request);
    }

    @PatchMapping("/{id}")
    public PublisherResponse patch(
            @PathVariable Long id,
            @Valid @RequestBody PublisherPatchRequest request
    ) {
        return publisherService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        publisherService.delete(id);
    }
}
