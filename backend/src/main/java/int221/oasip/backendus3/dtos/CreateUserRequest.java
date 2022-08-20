package int221.oasip.backendus3.dtos;

import int221.oasip.backendus3.entities.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateUserRequest {
    private String name;
    private String email;
    private Role role;
}
