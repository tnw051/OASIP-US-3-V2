package int221.oasip.backendus3.configs;

public interface JwtAuthenticationProviderConfigurer {
    void configure(MyJwtIssuerAuthenticationManagerResolver authenticationManagerResolver);
}
