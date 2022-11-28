package int221.oasip.backendus3.configs;

import com.azure.spring.cloud.autoconfigure.aad.AadTrustedIssuerRepository;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AadResourceServerConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtGrantedAuthoritiesConverter;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import com.querydsl.core.annotations.Config;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Configuration
public class AadConfiguration implements ProviderRegistrar {
    private final AadResourceServerProperties resourceServerProperties;
    private final AadAuthenticationProperties authProperties;
    private final AadResourceServerConfiguration aadResourceServerConfiguration;

    @Bean("aadJwtAuthenticationConverter")
    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        if (StringUtils.hasText(this.resourceServerProperties.getPrincipalClaimName())) {
            converter.setPrincipalClaimName(this.resourceServerProperties.getPrincipalClaimName());
        }

        converter.setJwtGrantedAuthoritiesConverter(new AadJwtGrantedAuthoritiesConverter(this.resourceServerProperties.getClaimToAuthorityPrefixMap()));
        return converter;
    }

    @Bean("aadJwtDecoder")
    private JwtDecoder jwtDecoder() {
        return aadResourceServerConfiguration.jwtDecoder(authProperties);
    }

    @Bean
    private AadTrustedIssuerRepository aadTrustedIssuerRepository() {
        return new AadTrustedIssuerRepository(authProperties.getProfile().getTenantId());
    }

    @Bean("aadJwtAuthenticationProvider")
    private JwtAuthenticationProvider jwtAuthenticationProvider() {
        JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider(jwtDecoder());
        authenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
        return authenticationProvider;
    }

    public void accept(MyJwtIssuerAuthenticationManagerResolver authenticationManagerResolver) {
        aadTrustedIssuerRepository().getTrustedIssuers().forEach(issuer -> authenticationManagerResolver.addManager(issuer, jwtAuthenticationProvider()));
    }

    public boolean isAadToken(Jwt jwt) {
        return aadTrustedIssuerRepository().getTrustedIssuers().contains(jwt.getIssuer().toString());
    }

    public String getEmail(Jwt jwt) {
        return jwt.getClaimAsString("preferred_username");
    }
}
