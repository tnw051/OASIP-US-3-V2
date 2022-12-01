package int221.oasip.backendus3.services.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUtil {
    private final AadJwtUtil aadJwtUtil;

    public AuthStatus getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication is null");
        }

        String email = null;
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            email = aadJwtUtil.isAadToken(jwt) ? aadJwtUtil.getEmail(jwt) : jwt.getClaimAsString("email");
        }

        return new AuthStatus(authentication, email);
    }
}
