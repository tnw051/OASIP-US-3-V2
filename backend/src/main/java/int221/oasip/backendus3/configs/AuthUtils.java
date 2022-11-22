package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.controllers.AuthStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtils {
    private final MyAadResourceServerUtils myAadResourceServerUtils;

    public AuthStatus getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication is null");
        }

        String email = null;
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            email = myAadResourceServerUtils.isAadToken(jwt) ? myAadResourceServerUtils.getEmail(jwt) : jwt.getClaimAsString("email");
        }

        return new AuthStatus(authentication, email);
    }
}
