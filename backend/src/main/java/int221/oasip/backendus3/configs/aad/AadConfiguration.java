package int221.oasip.backendus3.configs.aad;

import com.azure.spring.cloud.autoconfigure.aad.AadTrustedIssuerRepository;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AadResourceServerConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtGrantedAuthoritiesConverter;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import int221.oasip.backendus3.configs.JwtAuthenticationProviderConfigurer;
import int221.oasip.backendus3.configs.MyJwtIssuerAuthenticationManagerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Configuration
public class AadConfiguration implements JwtAuthenticationProviderConfigurer {
    private final AadResourceServerProperties resourceServerProperties;
    private final AadAuthenticationProperties authProperties;
    private final AadResourceServerConfiguration aadResourceServerConfiguration;

    @Override
    public void configure(MyJwtIssuerAuthenticationManagerResolver authenticationManagerResolver) {
        aadTrustedIssuerRepository().getTrustedIssuers()
                .forEach(issuer -> authenticationManagerResolver.addManager(issuer, jwtAuthenticationProvider()));
    }

    private JwtAuthenticationProvider jwtAuthenticationProvider() {
        JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider(jwtDecoder());
        authenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
        return authenticationProvider;
    }

    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        if (StringUtils.hasText(this.resourceServerProperties.getPrincipalClaimName())) {
            converter.setPrincipalClaimName(this.resourceServerProperties.getPrincipalClaimName());
        }

        converter.setJwtGrantedAuthoritiesConverter(new AadJwtGrantedAuthoritiesConverter(this.resourceServerProperties.getClaimToAuthorityPrefixMap()));
        return converter;
    }

    private JwtDecoder jwtDecoder() {
        return aadResourceServerConfiguration.jwtDecoder(authProperties);
    }

    @Bean
    public AadTrustedIssuerRepository aadTrustedIssuerRepository() {
        return new AadTrustedIssuerRepository(authProperties.getProfile().getTenantId());
    }
}
