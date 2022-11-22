package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.configs.MyUserDetails;
import int221.oasip.backendus3.dtos.LoginRequest;
import int221.oasip.backendus3.dtos.LoginResponse;
import int221.oasip.backendus3.dtos.MatchRequest;
import int221.oasip.backendus3.dtos.UserResponse;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.services.AuthService;
import int221.oasip.backendus3.services.TokenService;
import int221.oasip.backendus3.services.UserService;
import int221.oasip.backendus3.services.jwt.UserClaims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private final AuthService service;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;

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

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

        Jwt accessToken = tokenService.generateAccessToken(UserClaims.from(userDetails));
        Jwt refreshToken = tokenService.generateRefreshToken(UserClaims.from(userDetails));
        setRefreshTokenCookie(response, refreshToken);

        return new LoginResponse(accessToken.getTokenValue());
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is missing or has expired");
        }

        try {
            Jwt jwt = tokenService.decode(refreshToken);
            UserResponse user = Optional.of(jwt.getClaimAsString("id"))
                    .map(Integer::valueOf)
                    .map(userService::getById)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            Jwt accessToken = tokenService.generateAccessToken(UserClaims.from(user));
            return new LoginResponse(accessToken.getTokenValue());
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
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

    //    create a new user from Azure AD or do nothing if user already exists
    @PostMapping("/aad")
    public void authorizeAad(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String tid = jwt.getClaimAsString("tid");
        String oid = jwt.getClaimAsString("oid");
        String email = jwt.getClaimAsString("preferred_username");

        if (tid == null || oid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is missing tenantId or objectId");
        }

        userService.createAadUserIfNotExists(email, tid, oid);
    }


    @GetMapping("/private")
    public String hello() {
        return "What is he doing? LULW";
    }
}
