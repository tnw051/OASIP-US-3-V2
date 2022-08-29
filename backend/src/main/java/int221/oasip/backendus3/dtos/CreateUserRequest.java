package int221.oasip.backendus3.dtos;

import int221.oasip.backendus3.entities.Role;
import int221.oasip.backendus3.validators.EnumValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
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
    // extended java email regex by allowing leading and trailing whitespace
    // NOTE: @Email does not seem to work with the custom regex
    @Pattern(message = "Email is invalid", regexp = "\\s*(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])\\s*")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 14, message = "Password must be between {min} and {max} characters")
    private String password;

    @NotBlank(message = "Role must not be blank")
    @EnumValue(enumClass = Role.class, method = "fromString", message = "Role must be either student, admin, or lecturer")
    private String role = Role.STUDENT.toString();
}
