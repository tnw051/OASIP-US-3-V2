package int221.oasip.backendus3.repository;

import int221.oasip.backendus3.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer>, CustomEventRepository {
    @Query("SELECT E FROM Event E WHERE :categoryId = E.eventCategory.id AND " +
            "(:userId IS NULL OR :userId = E.user.id)")
    List<Event> findByEventCategory_IdAndUser_Id(Integer categoryId, @Nullable Integer userId);

    List<Event> findByUser_Id(Integer userId);

    List<Event> findByEventCategory_IdIn(Collection<Integer> categoryIds);
}