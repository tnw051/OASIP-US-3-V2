package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.dtos.LoginRequest;
import int221.oasip.backendus3.dtos.LoginResponse;
import int221.oasip.backendus3.dtos.MatchRequest;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private final AuthService service;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    @Value("${access-token.max-age-seconds}")
    private Long accessTokenMaxAgeSeconds;

    @Value("${refresh-token.max-age-seconds}")
    private Long refreshTokenMaxAgeSeconds;

    @Value("${refresh-token.secure}")
    private Boolean refreshTokenSecure;

    @PostMapping("/match")
    public String match(@Valid @RequestBody MatchRequest matchRequest) {
        try {
            boolean matches = service.match(matchRequest);
            if (!matches) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password NOT Matched");
            }
            return "Password Matched";
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials");
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        Jwt accessToken = generateAccessToken(authentication.getName());
        Jwt refreshToken = generateRefreshToken(authentication.getName());
        setRefreshTokenCookie(response, refreshToken);

        return new LoginResponse(accessToken.getTokenValue());
    }

    // refresh token endpoint
    @PostMapping("/refresh")
    public LoginResponse refresh(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is missing or has expired");
        }

        Jwt jwt;
        try {
            jwt = decoder.decode(refreshToken);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }

        Jwt accessToken = generateAccessToken(jwt.getSubject());

        return new LoginResponse(accessToken.getTokenValue());
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        deleteRefreshTokenCookie(response);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, Jwt refreshToken) {
        Cookie cookie = createBaseRefreshTokenCookie(refreshToken.getTokenValue());
        int expiry = (int) Duration.between(Instant.now(), refreshToken.getExpiresAt()).getSeconds();
        cookie.setMaxAge(expiry);
        response.addCookie(cookie);
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = createBaseRefreshTokenCookie(null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Cookie createBaseRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(refreshTokenSecure);
        cookie.setPath("/");
        return cookie;
    }

    private Jwt generateAccessToken(String subject) {
        Instant accessTokenExpiresAt = Instant.now().plusSeconds(accessTokenMaxAgeSeconds);
        JwtClaimsSet accessTokenClaims = createBaseClaimsSetBuilder()
                .subject(subject)
                .expiresAt(accessTokenExpiresAt)
                .build();

        return encodeTokenWithDefaultHeaders(accessTokenClaims);
    }

    private Jwt generateRefreshToken(String subject) {
        Instant refreshTokenExpiresAt = Instant.now().plusSeconds(refreshTokenMaxAgeSeconds);
        JwtClaimsSet refreshTokenClaims = createBaseClaimsSetBuilder()
                .subject(subject)
                .expiresAt(refreshTokenExpiresAt)
                .build();

        return encodeTokenWithDefaultHeaders(refreshTokenClaims);
    }

    private JwtClaimsSet.Builder createBaseClaimsSetBuilder() {
        return JwtClaimsSet.builder()
                .issuer("me smiley face")
                .issuedAt(Instant.now());
    }

    private Jwt encodeTokenWithDefaultHeaders(JwtClaimsSet claims) {
        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(headers, claims));
    }

    @GetMapping("/private")
    public String hello() {
        return "What is he doing? LULW";
    }
}

