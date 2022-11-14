package int221.oasip.backendus3.repository;


import int221.oasip.backendus3.entities.Event;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface CustomEventRepository {
    List<Event> findPastEvents(Instant startAt, @Nullable Collection<Integer> categoryIds, Integer userId);

    List<Event> findUpcomingAndOngoingEvents(Instant startAt, @Nullable Collection<Integer> categoryIds, @Nullable Integer userId);

    List<Event> findOverlapEventsByCategoryId(Instant startAt, Instant endAt, Integer categoryId, @Nullable Integer currentEventId);

//    List<Event> findByDateRange(Instant fromInclusive, Instant toExclusive, @Nullable List<Integer> categoryIds, Integer userId);

    List<Event> findByDateRangeOfOneDay(Instant startAt, @Nullable Collection<Integer> categoryIds, @Nullable Integer userId);
}
