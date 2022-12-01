package int221.oasip.backendus3.services.auth;

import com.azure.spring.cloud.autoconfigure.aad.AadTrustedIssuerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AadJwtUtil {
    private final AadTrustedIssuerRepository aadTrustedIssuerRepository;

    public boolean isAadToken(Jwt jwt) {
        return aadTrustedIssuerRepository.getTrustedIssuers().contains(jwt.getIssuer().toString());
    }

    public String getEmail(Jwt jwt) {
        return jwt.getClaimAsString("preferred_username");
    }
}
