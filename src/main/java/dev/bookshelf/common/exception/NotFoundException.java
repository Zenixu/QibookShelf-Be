package dev.bookshelf.common.exception;

/**
 * Data tidak ditemukan → HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
