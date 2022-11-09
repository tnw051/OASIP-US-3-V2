package int221.oasip.backendus3.configs;

import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtGrantedAuthoritiesConverter;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class MyAadResourceServerWebSecurityConfigurer {
    private final AadResourceServerProperties properties;

    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        if (StringUtils.hasText(this.properties.getPrincipalClaimName())) {
            converter.setPrincipalClaimName(this.properties.getPrincipalClaimName());
        }

        converter.setJwtGrantedAuthoritiesConverter(new AadJwtGrantedAuthoritiesConverter(this.properties.getClaimToAuthorityPrefixMap()));
        return converter;
    }
}
