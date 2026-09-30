package dev.bookshelf.publisher;

import dev.bookshelf.common.exception.ConflictException;
import dev.bookshelf.common.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class PublisherService {

    private final PublisherRepository publisherRepository;

    public PublisherService(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    public Page<PublisherResponse> list(String q, Pageable pageable) {
        Page<Publisher> page = StringUtils.hasText(q)
                ? publisherRepository.searchByName(q, pageable)
                : publisherRepository.findAll(pageable);
        return page.map(PublisherResponse::of);
    }

    public PublisherResponse getById(Long id) {
        return PublisherResponse.of(findPublisherOrThrow(id));
    }

    @Transactional
    public PublisherResponse create(PublisherRequest request) {
        if (publisherRepository.existsByName(request.name())) {
            throw new ConflictException("Penerbit '%s' sudah terdaftar".formatted(request.name()));
        }
        Publisher publisher = new Publisher(request.name(), request.city());
        return PublisherResponse.of(publisherRepository.save(publisher));
    }

    @Transactional
    public PublisherResponse update(Long id, PublisherRequest request) {
        Publisher publisher = findPublisherOrThrow(id);
        if (!publisher.getName().equals(request.name())
                && publisherRepository.existsByName(request.name())) {
            throw new ConflictException("Penerbit '%s' sudah terdaftar".formatted(request.name()));
        }
        publisher.setName(request.name());
        publisher.setCity(request.city());
        return PublisherResponse.of(publisher);
    }

    /**
     * PATCH /api/publishers/{id} — hanya field yang dikirim yang berubah.
     */
    @Transactional
    public PublisherResponse patch(Long id, PublisherPatchRequest request) {
        Publisher publisher = findPublisherOrThrow(id);
        if (request.hasName() && !publisher.getName().equals(request.name())) {
            if (publisherRepository.existsByName(request.name())) {
                throw new ConflictException("Penerbit '%s' sudah terdaftar".formatted(request.name()));
            }
            publisher.setName(request.name());
        }
        if (request.hasCity()) {
            publisher.setCity(request.city());
        }
        return PublisherResponse.of(publisher);
    }

    @Transactional
    public void delete(Long id) {
        Publisher publisher = findPublisherOrThrow(id);
        if (publisherRepository.isPublisherUsedByBooks(id)) {
            throw new ConflictException(
                    "Penerbit '%s' masih dipakai oleh buku; hapus bukunya terlebih dahulu"
                            .formatted(publisher.getName()));
        }
        publisherRepository.delete(publisher);
    }

    Publisher findPublisherOrThrow(Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Penerbit id %d tidak ditemukan".formatted(id)));
    }
}
