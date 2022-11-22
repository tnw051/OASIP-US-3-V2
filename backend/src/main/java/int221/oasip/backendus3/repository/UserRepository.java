package int221.oasip.backendus3.repository;

import int221.oasip.backendus3.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByProfileName(String name);

    Optional<User> findByProfileEmail(String email);

    Optional<User> findByAadUserTidAndAadUserOid(String tid, String oid);

    boolean existsByProfileName(String name);

    boolean existsByProfileEmail(String email);
}
