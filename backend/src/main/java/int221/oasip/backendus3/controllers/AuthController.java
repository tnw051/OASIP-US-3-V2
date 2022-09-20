package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.dtos.LoginRequest;
import int221.oasip.backendus3.dtos.LoginResponse;
import int221.oasip.backendus3.dtos.MatchRequest;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.services.AuthService;
import lombok.AllArgsConstructor;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private AuthService service;
    private AuthenticationManager authenticationManager;
    private JwtEncoder encoder;
    private JwtDecoder decoder;

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
        Instant now = Instant.now();
        Instant accessTokenExpiresAt = now.plus(30, ChronoUnit.MINUTES);
        Instant refreshTokenExpiresAt = now.plus(1, ChronoUnit.DAYS);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials");
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet baseClaims = JwtClaimsSet.builder()
                .issuer("me smiley face")
                .issuedAt(now)
                .subject(authentication.getName()).build();
        JwtClaimsSet accessTokenClaims = JwtClaimsSet.from(baseClaims)
                .expiresAt(accessTokenExpiresAt)
                .build();
        JwtClaimsSet refreshTokenClaims = JwtClaimsSet.from(baseClaims)
                .expiresAt(refreshTokenExpiresAt)
                .build();


        Jwt accessToken = encoder.encode(JwtEncoderParameters.from(headers, accessTokenClaims));
        Jwt refreshToken = encoder.encode(JwtEncoderParameters.from(headers, refreshTokenClaims));
        System.out.println(accessToken);
        System.out.println(refreshToken);

        Cookie cookie = new Cookie("refreshToken", refreshToken.getTokenValue());
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // TODO: change to false for development. Otherwise, the cookie will not be sent from postman
        cookie.setMaxAge((int) refreshTokenExpiresAt.getEpochSecond());
        response.addCookie(cookie);

        return new LoginResponse(accessToken.getTokenValue());
    }

    // refresh token endpoint
    @PostMapping("/refresh")
    public LoginResponse refresh(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        Instant now = Instant.now();
        Instant accessTokenExpiresAt = now.plus(30, ChronoUnit.MINUTES);

        Jwt jwt = decoder.decode(refreshToken);
        if (jwt.getExpiresAt() == null || jwt.getExpiresAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet baseClaims = JwtClaimsSet.builder()
                .issuer("me smiley face")
                .issuedAt(now)
                .subject(jwt.getSubject()).build();
        JwtClaimsSet accessTokenClaims = JwtClaimsSet.from(baseClaims)
                .expiresAt(accessTokenExpiresAt)
                .build();

        Jwt accessToken = encoder.encode(JwtEncoderParameters.from(headers, accessTokenClaims));

        return new LoginResponse(accessToken.getTokenValue());
    }

    @GetMapping("/private")
    public String hello() {
        return "What is he doing? LULW";
    }
}

