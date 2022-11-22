package int221.oasip.backendus3;

import int221.oasip.backendus3.dtos.UserResponse;
import int221.oasip.backendus3.entities.Profile;
import int221.oasip.backendus3.entities.Role;
import int221.oasip.backendus3.entities.User;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class MapperTest {
    @Test
    public void testMapper() {
        User user = new User();
        user.setId(123);
        Profile profile = new Profile();
        profile.setName("name");
        profile.setEmail("email");
        profile.setPassword("password");
        profile.setRole(Role.STUDENT);
        Instant now = Instant.now();
        profile.setCreatedOn(now);
        profile.setUpdatedOn(now);
        user.setProfile(profile);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(User.class, UserResponse.class).addMappings(mapper -> {
            mapper.map(User::getId, UserResponse::setId);
            mapper.map(src -> src.getProfile().getName(), UserResponse::setName);
            mapper.map(src -> src.getProfile().getEmail(), UserResponse::setEmail);
            mapper.map(src -> src.getProfile().getRole(), UserResponse::setRole);
            mapper.map(src -> src.getProfile().getCreatedOn(), UserResponse::setCreatedOn);
            mapper.map(src -> src.getProfile().getUpdatedOn(), UserResponse::setUpdatedOn);
        });
        UserResponse result = modelMapper.map(user, UserResponse.class);

        assertThat(result.getId()).isEqualTo(123);
        assertThat(result.getName()).isEqualTo("name");
        assertThat(result.getEmail()).isEqualTo("email");
        assertThat(result.getRole()).isEqualTo(Role.STUDENT);
        assertThat(result.getCreatedOn()).isEqualTo(now);
        assertThat(result.getUpdatedOn()).isEqualTo(now);
    }
}
