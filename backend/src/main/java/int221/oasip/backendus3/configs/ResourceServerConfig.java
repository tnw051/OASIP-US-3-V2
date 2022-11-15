package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class ResourceServerConfig extends AadResourceServerConfiguration.DefaultAadResourceServerWebSecurityConfigurerAdapter {
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {
    private final OasipJwtProps oasipJwtProps;
    private final MyAadResourceServerUtils myAadResourceServerUtils;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final MyJwtIssuerAuthenticationManagerResolver authenticationManagerResolver;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        addManagerForOasip();
        authenticationManagerResolver.register(myAadResourceServerUtils);

        super.configure(http);
        http
                .authorizeHttpRequests()
                .antMatchers("/api/auth/private").authenticated()
                .antMatchers("/api/users/**").hasAnyAuthority("ROLE_ADMIN", "APPROLE_Admin")
                .antMatchers("/api/auth/match").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/events").permitAll()
                .antMatchers("/api/events/test-lecturer").hasRole("LECTURER")
                .antMatchers(HttpMethod.GET, "/api/events/files/**").permitAll()
//                .antMatchers("/api/events/test").permitAll()
                .antMatchers("/api/events/**").authenticated()
                .anyRequest().permitAll()
                .and()
                .csrf().disable()
                .oauth2ResourceServer()
                .authenticationManagerResolver(authenticationManagerResolver)
                .and()
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    private void addManagerForOasip() {
        JwtAuthenticationProvider oasipAuthenticationProvider = new JwtAuthenticationProvider(tokenService.jwtDecoder());
        oasipAuthenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
        authenticationManagerResolver.addManager(oasipJwtProps.getIssueUri(), oasipAuthenticationProvider);
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("role");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
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
