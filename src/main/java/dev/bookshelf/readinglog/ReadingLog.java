package dev.bookshelf.readinglog;

import dev.bookshelf.book.Book;
import dev.bookshelf.security.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "reading_logs")
public class ReadingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReadingStatus status;

    @Column(name = "started_at")
    private LocalDate startedAt;

    @Column(name = "finished_at")
    private LocalDate finishedAt;

    /**
     * Diset SMALLINT agar sesuai dengan DDL Flyway (Types#SMALLINT);
     * Java Integer default dipetakan ke INTEGER dan gagal saat ddl-auto=validate.
     */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(columnDefinition = "smallint")
    private Integer rating;

    protected ReadingLog() {
    }

    public ReadingLog(Book book, ReadingStatus status, LocalDate startedAt, LocalDate finishedAt, Integer rating, User user) {
        this.book = book;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.rating = rating;
        this.user = user;
    }

    public Long getId() { return id; }
    public Book getBook() { return book; }
    public User getUser() { return user; }
    public ReadingStatus getStatus() { return status; }
    public LocalDate getStartedAt() { return startedAt; }
    public LocalDate getFinishedAt() { return finishedAt; }
    public Integer getRating() { return rating; }

    public void setStatus(ReadingStatus status) { this.status = status; }
    public void setStartedAt(LocalDate startedAt) { this.startedAt = startedAt; }
    public void setFinishedAt(LocalDate finishedAt) { this.finishedAt = finishedAt; }
    public void setRating(Integer rating) { this.rating = rating; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReadingLog other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : Objects.hash(id);
    }
}
