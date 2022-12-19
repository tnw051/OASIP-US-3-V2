package int221.oasip.backendus3.services.auth;

import lombok.Getter;
import org.springframework.security.core.Authentication;

@Getter
public class AuthStatus {
    public final boolean isAdmin;
    public final boolean isGuest;
    public final boolean isLecturer;
    public final boolean isStudent;
    public final String email;
    public final Authentication authentication;

    public AuthStatus(Authentication authentication, String email) {
        this.isAdmin = hasAnyAuthority(authentication, "ROLE_ADMIN", "APPROLE_Admin");
        this.isLecturer = hasAnyAuthority(authentication, "ROLE_LECTURER", "APPROLE_Lecturer");
        this.isStudent = hasAnyAuthority(authentication, "ROLE_STUDENT", "APPROLE_Student");
        this.isGuest = !this.isAdmin && !this.isLecturer && !this.isStudent;
        this.email = email;
        this.authentication = authentication;
    }

    private boolean hasAnyAuthority(Authentication authentication, String... authorities) {
        return authentication != null && authentication.getAuthorities().stream().anyMatch(a -> {
            for (String authority : authorities) {
                if (a.getAuthority().equals(authority)) {
                    return true;
                }
            }
            return false;
        });
    }
}
