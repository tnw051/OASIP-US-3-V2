package int221.oasip.backendus3.configs;

public interface ContextAwareAuthorizer<Resource, Context> {
    void check(Resource resource, Context context);
}
