package int221.oasip.backendus3.repository;

import int221.oasip.backendus3.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
