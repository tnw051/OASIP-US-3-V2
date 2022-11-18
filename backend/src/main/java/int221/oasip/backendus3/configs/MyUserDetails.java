package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.entities.User;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Getter
public class MyUserDetails extends org.springframework.security.core.userdetails.User {
    private final int id;
    private final String email;
    private final String name;
    private final String role;

    public MyUserDetails(User user) {
        super(user.getEmail(), user.getPassword(), List.of(new SimpleGrantedAuthority(user.getRole().name())));
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole().name();
    }
}
