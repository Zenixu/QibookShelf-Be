package dev.bookshelf.category;

import dev.bookshelf.common.exception.ConflictException;
import dev.bookshelf.common.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Page<CategoryResponse> list(String q, Pageable pageable) {
        Page<Category> page = StringUtils.hasText(q)
                ? categoryRepository.searchByName(q, pageable)
                : categoryRepository.findAll(pageable);
        return page.map(CategoryResponse::of);
    }

    public CategoryResponse getById(Long id) {
        return CategoryResponse.of(findCategoryOrThrow(id));
    }

    public CategoryResponse getBySlug(String slug) {
        return CategoryResponse.of(categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Kategori slug '%s' tidak ditemukan".formatted(slug))));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = resolveSlug(request);
        if (categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("Slug kategori '%s' sudah terdaftar".formatted(slug));
        }
        Category category = new Category(request.name(), slug);
        return CategoryResponse.of(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findCategoryOrThrow(id);
        String slug = resolveSlug(request);
        if (!category.getSlug().equals(slug) && categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("Slug kategori '%s' sudah terdaftar".formatted(slug));
        }
        category.setName(request.name());
        category.setSlug(slug);
        return CategoryResponse.of(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findCategoryOrThrow(id);
        if (categoryRepository.isCategoryUsedByBooks(id)) {
            throw new ConflictException(
                    "Kategori '%s' masih dipakai oleh buku; hapus bukunya terlebih dahulu"
                            .formatted(category.getName()));
        }
        categoryRepository.delete(category);
    }

    /**
     * Slug kustom dipakai apa adanya; kalau kosong diturunkan dari nama
     * (spasi → tanda hubung, hanya huruf kecil/angka).
     */
    private String resolveSlug(CategoryRequest request) {
        if (StringUtils.hasText(request.slug())) {
            return request.slug().trim().toLowerCase(Locale.ROOT);
        }
        return request.name().trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kategori id %d tidak ditemukan".formatted(id)));
    }
}
