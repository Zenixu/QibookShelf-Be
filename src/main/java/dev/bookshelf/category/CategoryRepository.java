package dev.bookshelf.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    java.util.Optional<Category> findBySlug(String slug);

    /** Pencarian nama parsial, case-insensitive. */
    @Query("""
            SELECT c FROM Category c
            WHERE :q IS NULL OR lower(c.name) LIKE lower(concat('%', :q, '%'))
            ORDER BY c.name
            """)
    Page<Category> searchByName(@Param("q") String q, Pageable pageable);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    @Query("""
            SELECT CASE WHEN COUNT(bc) > 0 THEN TRUE ELSE FALSE END
            FROM Book b JOIN b.categories bc
            WHERE bc.id = :id
            """)
    boolean isCategoryUsedByBooks(@Param("id") Long id);
}
