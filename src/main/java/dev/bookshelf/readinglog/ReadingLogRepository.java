package dev.bookshelf.readinglog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long> {

    @EntityGraph(attributePaths = {"book"})
    @Query("""
            SELECT rl FROM ReadingLog rl
            WHERE :status IS NULL OR rl.status = :status
            """)
    Page<ReadingLog> findAllByStatus(@Param("status") ReadingStatus status, Pageable pageable);

    /** Semua log milik satu user (multi-tenancy), terbaru di halaman pertama. */
    @EntityGraph(attributePaths = {"book"})
    Page<ReadingLog> findByUserId(Long userId, Pageable pageable);

    /** Log milik satu user dengan filter status, tanpa filter di memori. */
    @EntityGraph(attributePaths = {"book"})
    Page<ReadingLog> findByUserIdAndStatus(Long userId, ReadingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"book"})
    List<ReadingLog> findByBookIdOrderByStartedAtDesc(Long bookId);
}
