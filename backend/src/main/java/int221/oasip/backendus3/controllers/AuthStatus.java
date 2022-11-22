package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.entities.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import javax.annotation.Nullable;

@Getter
@RequiredArgsConstructor
public class AuthStatus {
    public final boolean isAdmin;
    public final boolean isGuest;
    public final boolean isLecturer;
    public final boolean isStudent;
    private final Role role;
    private final String name;
    private final String email;
    private final Authentication authentication;

    public AuthStatus(String name, String email, @Nullable Role role, Authentication authentication) {
        this.name = name;
        this.email = email;
        this.isAdmin = role == Role.ADMIN;
        this.isLecturer = role == Role.LECTURER;
        this.isStudent = role == Role.STUDENT;
        this.isGuest = role == null;
        this.role = role;
        this.authentication = authentication;
    }


}
