package int221.oasip.backendus3.configs;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oasip.jwt")
@Getter
@Setter
public class OasipJwtProps {
    private String issueUri;
}
