package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.configs.aad.AadConfiguration;
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

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AadConfiguration aadConfiguration;
    private final OasipJwtAuthenticationProviderConfigurer oasipJwtAuthenticationProviderConfigurer;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        MyJwtIssuerAuthenticationManagerResolver authenticationManagerResolver = new MyJwtIssuerAuthenticationManagerResolver();
        authenticationManagerResolver
                .register(aadConfiguration)
                .register(oasipJwtAuthenticationProviderConfigurer);

        http
                .authorizeHttpRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/auth/match").hasAnyAuthority("ROLE_ADMIN", "APPROLE_Admin")
                .antMatchers("/api/users/**").hasAnyAuthority("ROLE_ADMIN", "APPROLE_Admin")

                // @PreAuthorize is used on the controller to guard lecturer from create, update, delete events
                .antMatchers(HttpMethod.POST, "/api/events/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/events/allocatedTimeSlots").permitAll()
                .antMatchers("/api/events/**").authenticated()

                .antMatchers(HttpMethod.GET, "/api/categories/lecturer/**").hasAnyAuthority("ROLE_LECTURER", "APPROLE_Lecturer")
                .antMatchers(HttpMethod.GET, "/api/categories").permitAll()

                .antMatchers("/api/categories-owners").hasAnyAuthority("ROLE_ADMIN", "APPROLE_Admin")

                .antMatchers(HttpMethod.GET, "/api/files/**").permitAll()

                .anyRequest().authenticated()
                .and()
                .csrf().disable()
                .oauth2ResourceServer()
                .authenticationManagerResolver(authenticationManagerResolver)
                .and()
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
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
