package int221.oasip.backendus3.services;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import int221.oasip.backendus3.configs.OasipJwtProps;
import int221.oasip.backendus3.services.jwt.UserClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {
    public static final String HMAC_SHA_256 = "HmacSHA256";
    public static final String CLAIM_ID = "id";
    public static final String CLAIM_NAME = "name";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLE = "role";
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final String jwtSecret;
    private final OasipJwtProps jwtProps;
    private final Long accessTokenMaxAgeSeconds;
    private final Long refreshTokenMaxAgeSeconds;
    private final JwsHeader DEFAULT_JWS_HEADER = JwsHeader.with(MacAlgorithm.HS256).build();

    public TokenService(@Value("${jwt.secret}") String jwtSecret,
                        @Value("${access-token.max-age-seconds}") Long accessTokenMaxAgeSeconds,
                        @Value("${refresh-token.max-age-seconds}") Long refreshTokenMaxAgeSeconds,
                        OasipJwtProps jwtProps) {
        this.jwtSecret = jwtSecret;
        this.accessTokenMaxAgeSeconds = accessTokenMaxAgeSeconds;
        this.refreshTokenMaxAgeSeconds = refreshTokenMaxAgeSeconds;
        this.jwtProps = jwtProps;
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

    public Jwt generateAccessToken(UserClaims userClaims) {
        return createJwtFromUserClaims(userClaims, accessTokenMaxAgeSeconds);
    }

    public Jwt generateRefreshToken(UserClaims userClaims) {
        return createJwtFromUserClaims(userClaims, refreshTokenMaxAgeSeconds);
    }

    public Jwt createJwtFromUserClaims(UserClaims claims, Long maxAgeSeconds) {
        Instant expiresAt = Instant.now().plusSeconds(maxAgeSeconds);

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(jwtProps.getIssueUri())
                .subject(claims.getEmail())
                .claim(CLAIM_ID, claims.getId())
                .claim(CLAIM_NAME, claims.getName())
                .claim(CLAIM_EMAIL, claims.getEmail())
                .claim(CLAIM_ROLE, claims.getRole())
                .expiresAt(expiresAt)
                .issuedAt(Instant.now()).build();

        return encodeTokenWithDefaultHeaders(claimsSet);
    }

    public Jwt decode(String token) throws JwtException {
        return decoder.decode(token);
    }

    private Jwt encodeTokenWithDefaultHeaders(JwtClaimsSet claims) {
        return encoder.encode(JwtEncoderParameters.from(DEFAULT_JWS_HEADER, claims));
    }
}
