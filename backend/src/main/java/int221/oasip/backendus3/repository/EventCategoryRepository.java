package int221.oasip.backendus3.repository;

import int221.oasip.backendus3.entities.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventCategoryRepository extends JpaRepository<EventCategory, Integer> {
    EventCategory findByEventCategoryNameIgnoreCase(String name);

    List<EventCategory> findByOwners_User_Email(String email);
}
