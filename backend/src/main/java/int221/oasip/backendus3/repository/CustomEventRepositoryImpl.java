package int221.oasip.backendus3.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import int221.oasip.backendus3.entities.Event;
import int221.oasip.backendus3.entities.QEvent;
import org.springframework.lang.Nullable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class CustomEventRepositoryImpl implements CustomEventRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final QEvent event = QEvent.event;


    /**
     * Get past events (events that ended before or at the {@code startAt} time)
     * <p>{@code categoryIds} is optional. If it is not null, only events with category id in the list will be returned.
     * <p>{@code userId} is optional. If it is not null, only events with the user id will be returned.
     *
     * @param startAt     start time of event
     * @param categoryIds list of category ids
     * @param userId      user id of event
     * @return list of events that ended before or at the {@code startAt}
     */
    public List<Event> findPastEvents(Instant startAt, @Nullable List<Integer> categoryIds, Integer userId) {
        return getQueryWithCategoryIdsAndUserId(categoryIds, userId)
                .where(event.eventEndTime.loe(startAt))
                .fetch();
    }

        /**
     * Get upcoming and ongoing events (events that end after the {@code startAt} time)
     * <p>{@code categoryIds} is optional. If it is not null, only events with category id in the list will be returned.
     * <p>{@code userId} is optional. If it is not null, only events with the user id will be returned.
     *
     * @param startAt     start time of event
     * @param categoryIds list of category ids
     * @param userId      user id of event
     * @return list of events that started before the {@code startAt} or ended after the {@code startAt}
     */
    public List<Event> findUpcomingAndOngoingEvents(Instant startAt, @Nullable List<Integer> categoryIds, @Nullable Integer userId) {
        return getQueryWithCategoryIdsAndUserId(categoryIds, userId)
                .where(event.eventEndTime.gt(startAt))
                .fetch();
    }

    private JPAQuery<Event> getQueryWithCategoryIdsAndUserId(@Nullable List<Integer> categoryIds, @Nullable Integer userId) {
        return getQuery()
                .from(event)
                .where(withCategoryIdsAndUserId(categoryIds, userId));
    }

    private Predicate withCategoryIdsAndUserId(@Nullable List<Integer> categoryIds, @Nullable Integer userId) {
        BooleanExpression predicate = Expressions.TRUE.isTrue();
        if (categoryIds != null) {
            predicate = event.eventCategory.id.in(categoryIds);
        }
        if (userId != null) {
            predicate = predicate.and(event.user.id.eq(userId));
        }
        return predicate;
    }

    private JPAQuery<Event> getQuery() {
        return new JPAQuery<>(entityManager);
    }

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
    public List<Event> findOverlapEventsByCategoryId(Instant startAt, Instant endAt, Integer categoryId, @Nullable Integer currentEventId) {
        BooleanExpression isStartTimeOverlapped = event.eventStartTime.lt(startAt).and(event.eventEndTime.gt(startAt));
        BooleanExpression isEndTimeOverlapped = event.eventStartTime.goe(startAt).and(event.eventStartTime.lt(endAt));
        BooleanExpression isOverlapped = isStartTimeOverlapped.or(isEndTimeOverlapped);

        JPAQuery<Event> query = getQuery()
                .from(event)
                .where(event.eventCategory.id.eq(categoryId))
                .where(isOverlapped);

        if (currentEventId != null) {
            query.where(event.id.ne(currentEventId));
        }

        return query.fetch();


    }

    /**
     * Get all events that started in the selected day, starting from {@code startAt} (inclusive) to {@code startAt + 1 day} (exclusive)
     * <p>{@code categoryIds} is optional. If it is not null, only events with category id in the list will be returned.
     * <p>{@code userId} is optional. If it is not null, only events with the user id will be returned.
     *
     * @param startAt     start time of event
     * @param categoryIds list of category ids
     * @param userId      user id of event
     * @return list of events in the same day
     */
    public List<Event> findByDateRangeOfOneDay(Instant startAt, @Nullable List<Integer> categoryIds, @Nullable Integer userId) {
        Instant endAt = startAt.plus(1, ChronoUnit.DAYS);
        return findByDateRange(startAt, endAt, categoryIds, userId);
    }

    private List<Event> findByDateRange(Instant fromInclusive, Instant toExclusive, @Nullable List<Integer> categoryIds, @Nullable Integer userId) {
        return getQuery()
                .from(event)
                .where(event.eventStartTime.goe(fromInclusive))
                .where(event.eventStartTime.lt(toExclusive))
                .where(withCategoryIdsAndUserId(categoryIds, userId))
                .fetch();
    }
}
