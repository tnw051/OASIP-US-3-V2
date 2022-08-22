package int221.oasip.backendus3.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
public class CreateUserRequest {
    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must be less than {max} characters")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Size(max = 50, message = "Email must be less than {max} characters")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Role must not be blank")
    private String role;
}
