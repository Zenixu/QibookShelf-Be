package dev.bookshelf.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
