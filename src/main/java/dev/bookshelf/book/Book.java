package dev.bookshelf.book;

import dev.bookshelf.author.Author;
import dev.bookshelf.category.Category;
import dev.bookshelf.publisher.Publisher;
import dev.bookshelf.security.User;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 13, unique = true)
    private String isbn;

    @Column(name = "publish_year")
    private Integer publishYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    protected Book() {
    }

    public Book(String title, String isbn, Integer publishYear, Publisher publisher, User user) {
        this.title = title;
        this.isbn = isbn;
        this.publishYear = publishYear;
        this.publisher = publisher;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getIsbn() { return isbn; }
    public Integer getPublishYear() { return publishYear; }
    public User getUser() { return user; }
    public Publisher getPublisher() { return publisher; }
    public Set<Author> getAuthors() { return authors; }
    public Set<Category> getCategories() { return categories; }

    public void setTitle(String title) { this.title = title; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }
    public void setUser(User user) { this.user = user; }
    public void setPublisher(Publisher publisher) { this.publisher = publisher; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : Objects.hash(id);
    }
}
