package int221.oasip.backendus3.repository;

import int221.oasip.backendus3.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    /**
     * Get all overlap events. There are two scenarios:
     * <ol>
     *     <li>Events that started before the {@code startTime} and ended after the {@code startTime}</li>
     *     <li>Events that started between the {@code startTime} (inclusive) and the {@code endTime} (exclusive)</li>
     * </ol>
     * {@code currentEventId} is optional. If it is not null, the event with the id will be excluded from the result.
     *
     * @param startAt        start time of event
     * @param endAt          end time of event
     * @param categoryId     category id of event
     * @param currentEventId current event id
     * @return list of overlap events
     */
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM event e " +
                    "WHERE e.eventCategoryId = :categoryId " +
                    "AND (:currentEventId IS NULL OR e.eventId <> :currentEventId) AND " +
                    "((e.eventStartTime < :startAt AND TIMESTAMPADD(MINUTE, e.eventDuration, e.eventStartTime) > :startAt) OR " +
                    "(e.eventStartTime >= :startAt AND e.eventStartTime < :endAt))")
    List<Event> findOverlapEventsByCategoryId(Instant startAt, Instant endAt, Integer categoryId, @Nullable Integer currentEventId);

    @Query("SELECT E FROM Event E WHERE (:categoryId IS NULL OR E.eventCategory.id = :categoryId) AND " +
            "(:userId IS NULL OR E.user.id = :userId) AND " +
            "E.eventStartTime >= :fromInclusive AND E.eventStartTime < :toExclusive")
    List<Event> findByDateRange(Instant fromInclusive, Instant toExclusive, Integer categoryId, Integer userId);

    /**
     * Get all events that started in a range of one day, starting from {@code startAt} (inclusive) to {@code startAt + 1 day} (exclusive)
     * <p>{@code categoryId} is optional. If it is not null, only events with the category id will be returned.</p>
     * <p>{@code userId} is optional. If it is not null, only events with the user id will be returned.
     *
     * @param startAt    start time of event
     * @param categoryId category id of event
     * @param userId     user id of event
     * @return list of events in the same day
     */
    default List<Event> findByDateRangeOfOneDay(Instant startAt, @Nullable Integer categoryId, @Nullable Integer userId) {
        Instant endAt = startAt.plus(1, ChronoUnit.DAYS);
        return findByDateRange(startAt, endAt, categoryId, userId);
    }

    /**
     * Get all events in the specified category
     * <p>{@code userId} is optional. If it is not null, only events with the user id will be returned.
     *
     * @param categoryId category id of event
     * @param userId     user id of event
     * @return list of events in the category
     */
    //    List<Event> findByEventCategory_IdAndUser_Id(Integer categoryId, Integer userId);
    // refactor the above method to support optional userId
    @Query("SELECT E FROM Event E WHERE :categoryId = E.eventCategory.id AND " +
            "(:userId IS NULL OR :userId = E.user.id)")
    List<Event> findByEventCategory_IdAndUser_Id(Integer categoryId, @Nullable Integer userId);

    /**
     * Get upcoming and ongoing events (events that end after the {@code startAt} time)
     * <p>{@code categoryId} is optional. If it is not null, only events with the category id will be returned.
     * <p>{@code userId} is optional. If it is not null, only events with the user id will be returned.
     *
     * @param startAt    start time of event
     * @param categoryId category id of event
     * @param userId     user id of event
     * @return list of events that started before the {@code startAt} or ended after the {@code startAt}
     */
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM event e " +
                    "WHERE (:categoryId IS NULL OR e.eventCategoryId = :categoryId) AND " +
                    "(:userId IS NULL OR e.userId = :userId) AND " +
                    "TIMESTAMPADD(MINUTE, e.eventDuration, e.eventStartTime) > :startAt")
    List<Event> findUpcomingAndOngoingEvents(Instant startAt, @Nullable Integer categoryId, @Nullable Integer userId);

    /**
     * Get past events (events that ended before or at the {@code startAt} time)
     * <p>{@code categoryId} is optional. If it is not null, only events with the category id will be returned.
     * <p>{@code userId} is optional. If it is not null, only events with the user id will be returned.
     *
     * @param startAt    start time of event
     * @param categoryId category id of event
     * @param userId     user id of event
     * @return list of events that ended before or at the {@code startAt}
     */
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM event e " +
                    "WHERE (:categoryId IS NULL OR e.eventCategoryId = :categoryId) AND " +
                    "(:userId IS NULL OR e.userId = :userId) AND " +
                    "TIMESTAMPADD(MINUTE, e.eventDuration, e.eventStartTime) <= :startAt")
    List<Event> findPastEvents(Instant startAt, @Nullable Integer categoryId, Integer userId);

    List<Event> findByUser_Id(Integer userId);

//    List<Event> findByEventCategory_Lecturer_Email(String email);
}