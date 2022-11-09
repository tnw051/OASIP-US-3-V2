package int221.oasip.backendus3.services;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;

@Service
public class TokenService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    public static final String HMAC_SHA_256 = "HmacSHA256";
    private final String jwtSecret;

    public TokenService(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
        this.encoder = jwtEncoder();
        this.decoder = jwtDecoder();
    }

    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(jwtSecret.getBytes(), HMAC_SHA_256)).build();
        // remove the default 60 seconds clock skew
        OAuth2TokenValidator<Jwt> withoutClockSkew = new JwtTimestampValidator(Duration.ofSeconds(0));
        decoder.setJwtValidator(withoutClockSkew);
        return decoder;
    }

    public JwtEncoder jwtEncoder() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), HMAC_SHA_256);
        JWKSource<SecurityContext> immutableSecret = new ImmutableSecret<>(key);
        return new NimbusJwtEncoder(immutableSecret);
    }

    public JwtEncoder getEncoder() {
        return encoder;
    }

    public JwtDecoder getDecoder() {
        return decoder;
    }
}
