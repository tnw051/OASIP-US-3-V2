package int221.oasip.backendus3.dtos;

import int221.oasip.backendus3.entities.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UserResponse {
    private Integer id;
    private String name;
    private String email;
    private Role role;
    private Instant createdOn;
    private Instant updatedOn;
}
