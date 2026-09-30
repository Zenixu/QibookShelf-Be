package dev.bookshelf.author;

public record AuthorResponse(
        Long id,
        String name,
        String nationality
) {
    public static AuthorResponse of(Author a) {
        return new AuthorResponse(a.getId(), a.getName(), a.getNationality());
    }
}
