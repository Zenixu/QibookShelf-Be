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

    @EntityGraph(attributePaths = {"book"})
    List<ReadingLog> findByBookIdOrderByStartedAtDesc(Long bookId);

    @EntityGraph(attributePaths = {"book"})
    List<ReadingLog> findByBookIdAndUserIdOrderByStartedAtDesc(Long bookId, Long userId);

    @EntityGraph(attributePaths = {"book"})
    Page<ReadingLog> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"book"})
    Page<ReadingLog> findByUserIdAndStatus(Long userId, ReadingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"book"})
    java.util.Optional<ReadingLog> findByIdAndUserId(Long id, Long userId);
}
