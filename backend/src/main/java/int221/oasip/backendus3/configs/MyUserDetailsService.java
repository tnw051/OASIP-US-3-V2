package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.entities.User;
import int221.oasip.backendus3.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class MyUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByProfileEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new MyUserDetails(user.getProfile());
    }
}
