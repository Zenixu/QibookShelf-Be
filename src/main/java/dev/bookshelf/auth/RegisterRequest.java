package dev.bookshelf.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email tidak boleh kosong")
        @Email(message = "Format email tidak valid")
        String email,

        @NotBlank(message = "Username tidak boleh kosong")
        @Size(min = 3, max = 50, message = "Username harus 3-50 karakter")
        String username,

        @NotBlank(message = "Password tidak boleh kosong")
        @Size(min = 8, message = "Password minimal 8 karakter")
        String password,

        @Size(max = 150, message = "Nama lengkap maksimal 150 karakter")
        String fullName
) {
}
