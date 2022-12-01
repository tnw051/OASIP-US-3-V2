package int221.oasip.backendus3.configs;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class MyJwtIssuerAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {
    private final Map<String, AuthenticationManager> authenticationManagers = new HashMap<>();
    private final JwtIssuerAuthenticationManagerResolver authenticationManagerResolver = new JwtIssuerAuthenticationManagerResolver(authenticationManagers::get);

    public void addManager(String issuer, JwtAuthenticationProvider jwtAuthenticationProvider) {
        authenticationManagers.put(issuer, jwtAuthenticationProvider::authenticate);
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest authentication) {
        return authenticationManagerResolver.resolve(authentication);
    }

    public MyJwtIssuerAuthenticationManagerResolver register(JwtAuthenticationProviderConfigurer jwtAuthenticationProviderConfigurer) {
        jwtAuthenticationProviderConfigurer.configure(this);
        return this;
    }
}
