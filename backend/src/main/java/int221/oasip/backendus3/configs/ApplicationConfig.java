package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.utils.ModelMapperUtils;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public ModelMapperUtils modelMapperUtils() {
        return new ModelMapperUtils(modelMapper());
    }

    @Bean
    public ErrorAttributes errorAttributes() {
        return new ExtendedErrorAttributes();
    }
}
