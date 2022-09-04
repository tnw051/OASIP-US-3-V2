package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.dtos.LoginRequest;
import int221.oasip.backendus3.dtos.MatchRequest;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.Instant;

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
    public String login(@RequestBody LoginRequest loginRequest) {
        Instant now = Instant.now();
        long expiry = 36000L;

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials");
        }

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(authentication.getName())
//                .claim("scope", scope)
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

    }
}
