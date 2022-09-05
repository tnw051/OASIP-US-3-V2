package int221.oasip.backendus3.configs;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@RequiredArgsConstructor
public class ResourceServerConfig {
    public static final String HMAC_SHA_256 = "HmacSHA256";
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests()
                .antMatchers("/api/auth/private").authenticated()
                .antMatchers(HttpMethod.GET, "/api/users").authenticated()
                .anyRequest().permitAll()
                .and()
                .csrf().disable()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(jwtSecret.getBytes(), HMAC_SHA_256)).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), HMAC_SHA_256);
        JWKSource<SecurityContext> immutableSecret = new ImmutableSecret<>(key);
        return new NimbusJwtEncoder(immutableSecret);
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }
}
