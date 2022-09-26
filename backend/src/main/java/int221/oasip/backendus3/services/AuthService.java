package int221.oasip.backendus3.services;

import int221.oasip.backendus3.dtos.MatchRequest;
import int221.oasip.backendus3.entities.User;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private UserRepository userRepository;
    private Argon2PasswordEncoder argon2PasswordEncoder;

    public boolean match(MatchRequest matchRequest) {
        String strippedEmail = matchRequest.getEmail().strip();
        User user = userRepository.findByEmail(strippedEmail).orElseThrow(() -> new EntityNotFoundException("A user with the specified email DOES NOT exist"));

        return argon2PasswordEncoder.matches(matchRequest.getPassword(), user.getPassword());
    }
}
