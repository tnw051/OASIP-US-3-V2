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
import java.util.Collection;
import java.util.List;

public class CustomEventRepositoryImpl implements CustomEventRepository {

    private final QEvent event = QEvent.event;
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get past events (events that ended before or at the {@code startAt} time)
     * <p>{@code categoryIds} is optional. If it is not null, only events with category id in the list will be returned.
     * <p>{@code email} is optional. If it is not null, only events with that email will be returned.
     *
     * @param startAt     start time of event
     * @param categoryIds list of category ids
     * @param email       email of user
     * @return list of events that ended before or at the {@code startAt}
     */
    public List<Event> findPastEvents(Instant startAt, @Nullable Collection<Integer> categoryIds, String email) {
        return getQueryWithCategoryIdsAndEmail(categoryIds, email)
                .where(event.eventEndTime.loe(startAt))
                .fetch();
    }

    /**
     * Get upcoming and ongoing events (events that end after the {@code startAt} time)
     * <p>{@code categoryIds} is optional. If it is not null, only events with category id in the list will be returned.
     * <p>{@code email} is optional. If it is not null, only events with that email will be returned.
     *
     * @param startAt     start time of event
     * @param categoryIds list of category ids
     * @param email       email of user
     * @return list of events that started before the {@code startAt} or ended after the {@code startAt}
     */
    public List<Event> findUpcomingAndOngoingEvents(Instant startAt, @Nullable Collection<Integer> categoryIds, @Nullable String email) {
        return getQueryWithCategoryIdsAndEmail(categoryIds, email)
                .where(event.eventEndTime.gt(startAt))
                .fetch();
    }

    private JPAQuery<Event> getQueryWithCategoryIdsAndEmail(@Nullable Collection<Integer> categoryIds, @Nullable String email) {
        return getQuery()
                .from(event)
                .where(withCategoryIdsAndBookingEmail(categoryIds, email));
    }

    private Predicate withCategoryIdsAndBookingEmail(@Nullable Collection<Integer> categoryIds, @Nullable String email) {
        BooleanExpression predicate = Expressions.TRUE.isTrue();
        if (categoryIds != null) {
            predicate = event.eventCategory.id.in(categoryIds);
        }
        if (email != null) {
            predicate = predicate.and(event.bookingEmail.eq(email));
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

    @Override
    public List<Event> findByDateRangeOfOneDay(Instant startAt, Collection<Integer> categoryIds, String email) {
        return findByDateRangeOfOneDay(startAt, categoryIds, email, null);
    }

    /**
     * Get all events that started in the selected day, starting from {@code startAt} (inclusive) to {@code startAt + 1 day} (exclusive)
     * <p>{@code categoryIds} is optional. If it is not null, only events with category id in the list will be returned.
     * <p>{@code email} is optional. If it is not null, only events with that email will be returned.
     * <p>{@code excludeEventId} is optional. If it is not null, the event with the id will be excluded from the result.
     *
     * @param startAt        start time of event
     * @param categoryIds    list of category ids
     * @param email          email of user
     * @param excludeEventId event id to be excluded
     * @return list of events in the same day
     */
    public List<Event> findByDateRangeOfOneDay(Instant startAt, @Nullable Collection<Integer> categoryIds, @Nullable String email, @Nullable Integer excludeEventId) {
        Instant endAt = startAt.plus(1, ChronoUnit.DAYS);
        return findByDateRange(startAt, endAt, categoryIds, email, excludeEventId);
    }

    private List<Event> findByDateRange(Instant fromInclusive, Instant toExclusive, @Nullable Collection<Integer> categoryIds, @Nullable String email, @Nullable Integer excludeEventId) {
        JPAQuery<Event> query = getQuery()
                .from(event)
                .where(event.eventStartTime.goe(fromInclusive))
                .where(event.eventStartTime.lt(toExclusive))
                .where(withCategoryIdsAndBookingEmail(categoryIds, email));

        if (excludeEventId != null) {
            query.where(event.id.ne(excludeEventId));
        }

        return query.fetch();
    }
}
