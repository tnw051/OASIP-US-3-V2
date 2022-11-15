package int221.oasip.backendus3.configs;

public interface ProviderRegistrar {
    void registerProvider(MyJwtIssuerAuthenticationManagerResolver authenticationManagerResolver);
}
