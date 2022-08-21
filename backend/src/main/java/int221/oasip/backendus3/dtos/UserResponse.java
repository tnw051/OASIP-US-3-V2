package int221.oasip.backendus3.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UserResponse {
    private String name;
    private String email;
    private String role;
    private Instant createdOn;
    private Instant updatedOn;
}
