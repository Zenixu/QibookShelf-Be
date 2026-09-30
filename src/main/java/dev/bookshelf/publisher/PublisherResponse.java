package dev.bookshelf.publisher;

public record PublisherResponse(
        Long id,
        String name,
        String city
) {
    public static PublisherResponse of(Publisher p) {
        return new PublisherResponse(p.getId(), p.getName(), p.getCity());
    }
}
