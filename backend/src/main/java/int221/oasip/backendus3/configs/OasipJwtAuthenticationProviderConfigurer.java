package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class OasipJwtAuthenticationProviderConfigurer implements JwtAuthenticationProviderConfigurer {
    private final TokenService tokenService;
    private final OasipJwtProps oasipJwtProps;

    @Override
    public void configure(MyJwtIssuerAuthenticationManagerResolver authenticationManagerResolver) {
        authenticationManagerResolver.addManager(oasipJwtProps.getIssueUri(), getJwtAuthenticationProvider());
    }

    private JwtAuthenticationProvider getJwtAuthenticationProvider() {
        JwtAuthenticationProvider oasipAuthenticationProvider = new JwtAuthenticationProvider(tokenService.jwtDecoder());
        oasipAuthenticationProvider.setJwtAuthenticationConverter(getJwtAuthenticationConverter());
        return oasipAuthenticationProvider;
    }

    private JwtAuthenticationConverter getJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("role");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
