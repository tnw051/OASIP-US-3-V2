package int221.oasip.backendus3.configs;

import com.azure.spring.cloud.autoconfigure.aad.AadTrustedIssuerRepository;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AadResourceServerConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtGrantedAuthoritiesConverter;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class MyAadResourceServerUtils implements ProviderRegistrar {
    private final AadResourceServerProperties resourceServerProperties;
    private final AadAuthenticationProperties authProperties;
    private final AadResourceServerConfiguration aadResourceServerConfiguration;
    private AadTrustedIssuerRepository aadTrustedIssuerRepository;
    private JwtDecoder jwtDecoder;
    private JwtAuthenticationConverter converter;

    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        if (converter == null) {
            converter = new JwtAuthenticationConverter();
            if (StringUtils.hasText(this.resourceServerProperties.getPrincipalClaimName())) {
                converter.setPrincipalClaimName(this.resourceServerProperties.getPrincipalClaimName());
            }

            converter.setJwtGrantedAuthoritiesConverter(new AadJwtGrantedAuthoritiesConverter(this.resourceServerProperties.getClaimToAuthorityPrefixMap()));
        }
        return converter;
    }

    private JwtDecoder jwtDecoder() {
        if (jwtDecoder == null) {
            jwtDecoder = aadResourceServerConfiguration.jwtDecoder(authProperties);
        }
        return jwtDecoder;
    }

    private AadTrustedIssuerRepository aadTrustedIssuerRepository() {
        if (aadTrustedIssuerRepository == null) {
            aadTrustedIssuerRepository = new AadTrustedIssuerRepository(authProperties.getProfile().getTenantId());
        }
        return aadTrustedIssuerRepository;
    }

    public JwtAuthenticationProvider jwtAuthenticationProvider() {
        JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider(jwtDecoder());
        authenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
        return authenticationProvider;
    }

    public void registerProvider(MyJwtIssuerAuthenticationManagerResolver authenticationManagerResolver) {
        aadTrustedIssuerRepository().getTrustedIssuers().forEach(issuer -> authenticationManagerResolver.addManager(issuer, jwtAuthenticationProvider()));
    }
}
