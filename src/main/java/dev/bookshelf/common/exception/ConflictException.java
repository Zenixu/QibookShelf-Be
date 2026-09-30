package dev.bookshelf.common.exception;

/**
 * Konflik data → HTTP 409. Dipakai untuk nilai unik duplikat (ISBN, nama, slug)
 * atau penghapusan data yang masih dipakai oleh buku.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
