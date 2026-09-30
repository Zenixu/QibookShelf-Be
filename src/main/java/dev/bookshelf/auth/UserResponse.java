package dev.bookshelf.auth;

import dev.bookshelf.security.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String username,
        String fullName,
        Boolean isActive,
        LocalDateTime createdAt
) {
    public static UserResponse of(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getActive(),
                user.getCreatedAt()
        );
    }
}
