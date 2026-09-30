package dev.bookshelf.common;

import dev.bookshelf.security.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class untuk mendapatkan user yang sedang login.
 */
public final class SecurityUtil {

    private SecurityUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Mendapatkan user yang sedang login dari SecurityContext.
     * 
     * @return User yang sedang login
     * @throws IllegalStateException jika user tidak terautentikasi
     */
    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof User)) {
            throw new IllegalStateException("User tidak terautentikasi");
        }
        return (User) auth.getPrincipal();
    }

    /**
     * Mendapatkan ID user yang sedang login.
     * 
     * @return ID user yang sedang login
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
