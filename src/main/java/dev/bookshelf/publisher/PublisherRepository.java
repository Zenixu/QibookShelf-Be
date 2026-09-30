package dev.bookshelf.publisher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    @Query("""
            SELECT p FROM Publisher p
            WHERE :q IS NULL OR lower(p.name) LIKE lower(concat('%', :q, '%'))
            ORDER BY p.name
            """)
    Page<Publisher> searchByName(@Param("q") String q, Pageable pageable);

    boolean existsByName(String name);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END
            FROM Book b
            WHERE b.publisher.id = :id
            """)
    boolean isPublisherUsedByBooks(@Param("id") Long id);
}
