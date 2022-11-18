package int221.oasip.backendus3.services.jwt;

import int221.oasip.backendus3.configs.MyUserDetails;
import int221.oasip.backendus3.dtos.UserResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserClaims {
    private final int id;
    private final String email;
    private final String name;
    private final String role;

    public static UserClaims from(UserResponse userResponse) {
        return new UserClaims(userResponse.getId(), userResponse.getEmail(), userResponse.getName(), userResponse.getRole().name());
    }

    public static UserClaims from(MyUserDetails myUserDetails) {
        return new UserClaims(myUserDetails.getId(), myUserDetails.getEmail(), myUserDetails.getName(), myUserDetails.getRole());
    }
}
