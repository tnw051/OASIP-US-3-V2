package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.controllers.AuthStatus;
import int221.oasip.backendus3.entities.Role;
import int221.oasip.backendus3.entities.User;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthUtils {
    //    private final Map<String, JwtAuthInfoResolver> authInfoResolvers = new HashMap<>();
    private final MyAadResourceServerUtils myAadResourceServerUtils;
    private final UserRepository userRepository;

    private static AuthStatus getOasipAuthStatus(Authentication authentication, Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        Role role = Role.tryFromString(jwt.getClaimAsString("role"));
        return new AuthStatus(name, email, role, authentication);
    }

    private static AuthStatus getAadAuthStatus(Authentication authentication, Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        String email = jwt.getClaimAsString("preferred_username");
        Role role = Role.tryFromString(jwt.getClaimAsStringList("roles").get(0));

        return new AuthStatus(name, email, role, authentication);
    }

    public AuthStatus getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication is null");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        if (myAadResourceServerUtils.isAadToken(jwt)) {
            return getAadAuthStatus(authentication, jwt);
        }

        return getOasipAuthStatus(authentication, jwt);
    }

    public User getCurrentUserOrThrow() {
        AuthStatus authStatus = getAuthStatus();

        Optional<User> oasipUser = userRepository.findByProfileEmail(authStatus.getEmail());
        if (oasipUser.isPresent()) {
            return oasipUser.get();
        }

        Object principal = authStatus.getAuthentication().getPrincipal();
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            if (myAadResourceServerUtils.isAadToken(jwt)) {
                Optional<User> aadUser = userRepository.findByAadUserTidAndAadUserOid(jwt.getClaim("tid"), jwt.getClaim("oid"));
                if (aadUser.isPresent()) {
                    return aadUser.get();
                }
            }
        }

        throw new EntityNotFoundException("User not found");
    }
}
