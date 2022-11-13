package int221.oasip.backendus3.configs;

import org.hibernate.dialect.MySQL8Dialect;

@SuppressWarnings("unused")
public class ExtendedMySQL8Dialect extends MySQL8Dialect {
    public ExtendedMySQL8Dialect() {
        super();
        registerKeyword("MINUTE");
    }
}
