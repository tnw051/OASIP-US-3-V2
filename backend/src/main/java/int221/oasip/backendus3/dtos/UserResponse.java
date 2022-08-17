package int221.oasip.backendus3.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Getter
@Setter
public class UserResponse {
    private String name;
    private String email;
    private String role;
}
