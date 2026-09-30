package dev.bookshelf.category;

public record CategoryResponse(
        Long id,
        String name,
        String slug
) {
    public static CategoryResponse of(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug());
    }
}
