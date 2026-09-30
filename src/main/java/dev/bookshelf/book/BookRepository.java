package dev.bookshelf.book;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /**
     * Detail satu buku: publisher + authors + categories dimuat dalam satu query (anti N+1).
     */
    @EntityGraph(attributePaths = {"publisher", "authors", "categories"})
    Optional<Book> findWithDetailsById(Long id);

    Optional<Book> findByIsbn(String isbn);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END
            FROM Book b
            WHERE lower(b.title) = lower(:title)
            """)
    boolean existsByTitleIgnoreCase(@Param("title") String title);
}
