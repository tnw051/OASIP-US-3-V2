package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.dtos.UserResponse;
import int221.oasip.backendus3.entities.User;
import int221.oasip.backendus3.utils.ModelMapperUtils;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

@Configuration
public class ApplicationConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(User.class, UserResponse.class).addMappings(mapper -> {
            mapper.map(User::getId, UserResponse::setId);
            mapper.map(src -> src.getProfile().getName(), UserResponse::setName);
            mapper.map(src -> src.getProfile().getEmail(), UserResponse::setEmail);
            mapper.map(src -> src.getProfile().getRole(), UserResponse::setRole);
            mapper.map(src -> src.getProfile().getCreatedOn(), UserResponse::setCreatedOn);
            mapper.map(src -> src.getProfile().getUpdatedOn(), UserResponse::setUpdatedOn);
        });

        return modelMapper;
    }

    @Bean
    public ModelMapperUtils modelMapperUtils() {
        return new ModelMapperUtils(modelMapper());
    }

    @Bean
    public Argon2PasswordEncoder argon2PasswordEncoder() {
        return new Argon2PasswordEncoder();
    }
}
