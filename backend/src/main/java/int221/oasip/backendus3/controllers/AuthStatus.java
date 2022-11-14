package int221.oasip.backendus3.controllers;

import lombok.Getter;
import org.springframework.security.core.Authentication;

@Getter
public class AuthStatus {
    public final boolean isAdmin;
    public final boolean isGuest;
    public final boolean isLecturer;
    public final boolean isStudent;

    public AuthStatus(Authentication authentication) {
        this.isAdmin = authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        this.isGuest = authentication == null;
        this.isLecturer = authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LECTURER"));
        this.isStudent = authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
    }
}
