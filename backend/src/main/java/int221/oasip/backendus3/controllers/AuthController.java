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
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(1, ChronoUnit.DAYS);

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
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("me smiley face")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(authentication.getName())
                .build();

        Jwt token = encoder.encode(JwtEncoderParameters.from(headers, claims));
        return new LoginResponse(token.getTokenValue());
    }

    @GetMapping("/private")
    public String hello() {
        return "What is he doing? LULW";
    }
}
