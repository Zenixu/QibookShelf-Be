package dev.bookshelf.author;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    /** Pencarian nama parsial, case-insensitive (untuk GET /api/authors?q=). */
    @Query("""
            SELECT a FROM Author a
            WHERE :q IS NULL OR lower(a.name) LIKE lower(concat('%', :q, '%'))
            ORDER BY a.name
            """)
    Page<Author> searchByName(@Param("q") String q, Pageable pageable);

    /** Cek nama yang sama persis (case-insensitive) untuk validasi duplikat. */
    boolean existsByName(String name);

    @Query("""
            SELECT CASE WHEN COUNT(ba) > 0 THEN TRUE ELSE FALSE END
            FROM Book b JOIN b.authors ba
            WHERE ba.id = :id
            """)
    boolean isAuthorUsedByBooks(@Param("id") Long id);
}
