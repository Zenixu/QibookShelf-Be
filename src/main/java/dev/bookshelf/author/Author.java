package dev.bookshelf.author;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 80)
    private String nationality;

    protected Author() {
    }

    public Author(String name, String nationality) {
        this.name = name;
        this.nationality = nationality;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getNationality() { return nationality; }

    public void setName(String name) { this.name = name; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        // id null = entity belum dipersist; pakai identitas objek agar aman di Set sebelum save
        return id == null ? System.identityHashCode(this) : Objects.hash(id);
    }
}
