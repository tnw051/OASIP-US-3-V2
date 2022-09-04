package int221.oasip.backendus3.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String type = "Bearer";
    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }
}
