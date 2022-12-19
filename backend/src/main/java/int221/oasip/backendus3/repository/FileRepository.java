package int221.oasip.backendus3.repository;

import int221.oasip.backendus3.entities.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Integer> {
}