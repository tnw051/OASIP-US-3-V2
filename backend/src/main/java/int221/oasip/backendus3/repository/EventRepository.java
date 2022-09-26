package int221.oasip.backendus3.repository;

import int221.oasip.backendus3.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    // get all overlap events. there are two scenarios:
    // 1. events that started before the startTime and ended after the startTime
    // 2. events that started between the startTime (inclusive) and the endTime (exclusive)
    // with optional currentEventId to exclude the current event from the result
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM event e " +
                    "WHERE e.eventCategoryId = :categoryId " +
                    // optional eventId to exclude from the result
                    "AND (:currentEventId IS NULL OR e.eventId <> :currentEventId) AND " +
                    // case 1
                    "((e.eventStartTime < :startAt AND TIMESTAMPADD(MINUTE, e.eventDuration, e.eventStartTime) > :startAt) OR " +
                    // case 2
                    "(e.eventStartTime >= :startAt AND e.eventStartTime < :endAt))")
    List<Event> findOverlapEventsByCategoryId(Instant startAt, Instant endAt, Integer categoryId, Integer currentEventId);

    default List<Event> findOverlapEventsByCategoryId(Instant startAt, Instant endAt, Integer categoryId) {
        return findOverlapEventsByCategoryId(startAt, endAt, categoryId, null);
    }

    // optional categoryId
    @Query("SELECT E FROM Event E WHERE (?1 IS NULL OR E.eventCategory.id = ?1) AND E.eventStartTime >= ?2 AND E.eventStartTime < ?3")
    List<Event> findByDateRange(Integer categoryId, Instant fromInclusive, Instant toExclusive);

    default List<Event> findByDateRangeOfOneDay(Instant startAt, Integer categoryId) {
        Instant endAt = startAt.plus(1, ChronoUnit.DAYS);
        return findByDateRange(categoryId, startAt, endAt);
    }

    default List<Event> findByDateRangeOfOneDay(Instant startAt) {
        return findByDateRangeOfOneDay(startAt, null);
    }

    List<Event> findByEventCategory_Id(Integer categoryId);

    // get upcoming and ongoing events, with these constraints:
    // 1. categoryId is optional.
    // 2. events that start after startAt (upcoming)
    // 3. event that start before startAt but end after startAt (ongoing)
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM event e " +
                    "WHERE (:categoryId IS NULL OR e.eventCategoryId = :categoryId) AND " +
                    // upcoming
                    "((e.eventStartTime > :startAt) OR " +
                    // ongoing
                    "(e.eventStartTime <= :startAt AND TIMESTAMPADD(MINUTE, e.eventDuration, e.eventStartTime) > :startAt))")
    List<Event> findUpcomingAndOngoingEvents(Instant startAt, Integer categoryId);

    default List<Event> findUpcomingAndOngoingEvents(Instant startAt) {
        return findUpcomingAndOngoingEvents(startAt, null);
    }

    // get past events (start before startAt and end at or before startAt)
    @Query(nativeQuery = true,
            value = "SELECT * FROM event e WHERE (:categoryId IS NULL OR e.eventCategoryId = :categoryId) AND " +
                    "(e.eventStartTime < :startAt AND TIMESTAMPADD(MINUTE, e.eventDuration, e.eventStartTime) <= :startAt)")
    List<Event> findPastEvents(Instant startAt, Integer categoryId);

    default List<Event> findPastEvents(Instant now) {
        return findPastEvents(now, null);
    }
}