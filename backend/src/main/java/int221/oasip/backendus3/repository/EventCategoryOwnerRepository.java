package int221.oasip.backendus3.repository;

import int221.oasip.backendus3.entities.EventCategoryOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventCategoryOwnerRepository extends JpaRepository<EventCategoryOwner, Integer> {
}
