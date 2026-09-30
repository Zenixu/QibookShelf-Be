package dev.bookshelf.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Pembungkus respons paginasi seragam untuk semua endpoint list (lihat API-CONTRACT.md).
 *
 * @param content       daftar isi halaman
 * @param page          nomor halaman (mulai dari 0)
 * @param size          ukuran halaman
 * @param totalElements jumlah total seluruh data
 * @param totalPages    jumlah total halaman
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
