package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.entities.Profile;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Getter
public class MyUserDetails extends org.springframework.security.core.userdetails.User {
    private final int id;
    private final String email;
    private final String name;
    private final String role;

    public MyUserDetails(Profile profile) {
        super(profile.getEmail(), profile.getPassword(), List.of(new SimpleGrantedAuthority(profile.getRole().name())));
        this.id = profile.getId();
        this.email = profile.getEmail();
        this.name = profile.getName();
        this.role = profile.getRole().name();
    }
}
