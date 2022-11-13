package int221.oasip.backendus3.repository;

import int221.oasip.backendus3.entities.Event;
import int221.oasip.backendus3.entities.EventCategory;
import int221.oasip.backendus3.entities.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application-test.properties")
// splitting into subtests could be better, but the cost may not be worth it for app this small
class EventRepositoryTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2022-05-05T08:00:00Z"), ZoneId.systemDefault());
    private final Instant NOW = FIXED_CLOCK.instant();
    @Autowired
    EventCategoryRepository categoryRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    private static EventCategory category15MinutesA;
    private static EventCategory category15MinutesB;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeAll
    void setUp() {
        category15MinutesA = categoryRepository.save(new EventCategory("15 minutes A", "category description", 15));
        category15MinutesB = categoryRepository.save(new EventCategory("15 minutes B", "category description", 15));
        System.out.println("BeforeEach outer");
        for (EventCategory category : categoryRepository.findAll()) {
            System.out.println("category = " + category.getId());
        }
        System.out.println("End of BeforeEach outer");
    }

    @Test
    void findPastEvents_returnPastEvents() {
        Event eventEndedBeforeNow = createEventAndSave(category15MinutesA, NOW.minus(1, ChronoUnit.DAYS));
        Event eventEndedJustNow = createEventAndSave(category15MinutesA, NOW.minus(category15MinutesA.getEventDuration(), ChronoUnit.MINUTES));
        Event eventToStartInOneDay = createEventAndSave(category15MinutesA, NOW.plus(1, ChronoUnit.DAYS));
        Event eventToStartInOneMinute = createEventAndSave(category15MinutesA, NOW.plus(1, ChronoUnit.MINUTES));

        List<Event> events = eventRepository.findPastEvents(NOW, null, null);

        assertThat(events).containsExactlyInAnyOrder(eventEndedBeforeNow, eventEndedJustNow);
    }

    @Nested
    class FindPastEvents_CombinationOfCategoryAndUser {
        private Event eventA;
        private Event eventB;

        @BeforeEach
        void init() {
            eventA = createEventAndSave(category15MinutesA, NOW.minus(1, ChronoUnit.DAYS), 1);
            eventB = createEventAndSave(category15MinutesB, NOW.minus(1, ChronoUnit.DAYS), 2);
        }

        @Test
        void returnEventsInACategory() {
            List<Event> events = eventRepository.findPastEvents(NOW, List.of(category15MinutesA.getId()), null);
            assertThat(events).containsExactlyInAnyOrder(eventA);
        }

        @Test
        void returnEventsInMultipleCategories() {
            List<Event> events = eventRepository.findPastEvents(NOW, List.of(category15MinutesA.getId(), category15MinutesB.getId()), null);
            assertThat(events).containsExactlyInAnyOrder(eventA, eventB);
        }

        @Test
        void returnEventsForAUser() {
            List<Event> events = eventRepository.findPastEvents(NOW, null, 1);
            assertThat(events).containsExactlyInAnyOrder(eventA);
        }

        @Test
        void returnEventsForAUserInMultipleCategories() {
            List<Event> events = eventRepository.findPastEvents(NOW, List.of(category15MinutesA.getId(), category15MinutesB.getId()), 1);
            assertThat(events).containsExactlyInAnyOrder(eventA);
        }

        @Test
        void returnAllEvents() {
            List<Event> events = eventRepository.findPastEvents(NOW, null, null);
            assertThat(events).containsExactlyInAnyOrder(eventA, eventB);
        }
    }

    @Test
    void findUpcomingAndOngoingEvents_returnUpcomingAndOngoingEvents() {
        Event eventEndedBeforeNow = createEventAndSave(category15MinutesA, NOW.minus(1, ChronoUnit.DAYS));
        Event eventEndedJustNow = createEventAndSave(category15MinutesA, NOW.minus(category15MinutesA.getEventDuration(), ChronoUnit.MINUTES));
        Event eventToStartInOneDay = createEventAndSave(category15MinutesA, NOW.plus(1, ChronoUnit.DAYS));
        Event eventToStartInOneMinute = createEventAndSave(category15MinutesA, NOW.plus(1, ChronoUnit.MINUTES));
        Event eventStartedOneMinuteAgo = createEventAndSave(category15MinutesA, NOW.minus(1, ChronoUnit.MINUTES));
        Event eventStartedJustNow = createEventAndSave(category15MinutesA, NOW);

        List<Event> events = eventRepository.findUpcomingAndOngoingEvents(NOW, null, null);

        assertThat(events).containsExactlyInAnyOrder(eventToStartInOneDay, eventToStartInOneMinute, eventStartedOneMinuteAgo, eventStartedJustNow);
    }

    @Nested
    class FindOverlapEventsByCategoryId {
        Instant startAt = NOW;
        EventCategory category = category15MinutesA;
        EventCategory unrelatedCategory = category15MinutesB;
        Instant endAt = startAt.plus(category.getEventDuration(), ChronoUnit.MINUTES);
        Event eventStartedBeforeStartAtAndEndedAfterStartAt;
        Event eventStartedBeforeEndAtAndEndedAfterEndAt;
        Event eventEndedBeforeStartAt;
        Event eventStartedAfterEndAt;
        Event eventStartedBeforeStartAtAndEndedAfterStartAt_unrelatedCategory;
        Event eventStartedBeforeEndAtAndEndedAfterEndAt_unrelatedCategory;
        Event eventEndedBeforeStartAt_unrelatedCategory;
        Event eventStartedAfterEndAt_unrelatedCategory;

        @BeforeEach
        void init() {
            eventStartedBeforeStartAtAndEndedAfterStartAt = createEventAndSave(category, startAt.minus(1, ChronoUnit.MINUTES));
            eventStartedBeforeEndAtAndEndedAfterEndAt = createEventAndSave(category, endAt.minus(1, ChronoUnit.MINUTES));
            eventEndedBeforeStartAt = createEventAndSave(category, startAt.minus(1, ChronoUnit.DAYS));
            eventStartedAfterEndAt = createEventAndSave(category, endAt.plus(1, ChronoUnit.DAYS));

            eventStartedBeforeStartAtAndEndedAfterStartAt_unrelatedCategory = createEventAndSave(unrelatedCategory, startAt.minus(1, ChronoUnit.MINUTES));
            eventStartedBeforeEndAtAndEndedAfterEndAt_unrelatedCategory = createEventAndSave(unrelatedCategory, endAt.minus(1, ChronoUnit.MINUTES));
            eventEndedBeforeStartAt_unrelatedCategory = createEventAndSave(unrelatedCategory, startAt.minus(1, ChronoUnit.DAYS));
            eventStartedAfterEndAt_unrelatedCategory = createEventAndSave(unrelatedCategory, endAt.plus(1, ChronoUnit.DAYS));
        }

        @Test
        void returnOverlappedEventsInACategory() {
            List<Event> events = eventRepository.findOverlapEventsByCategoryId(startAt, endAt, category.getId(), null);
            assertThat(events).containsExactlyInAnyOrder(eventStartedBeforeStartAtAndEndedAfterStartAt, eventStartedBeforeEndAtAndEndedAfterEndAt);
        }

        @Test
        void returnOverlappedEventsInACategoryExcludingCurrentEvent() {
            List<Event> events = eventRepository.findOverlapEventsByCategoryId(startAt, endAt, category.getId(), eventStartedBeforeStartAtAndEndedAfterStartAt.getId());
            assertThat(events).containsExactlyInAnyOrder(eventStartedBeforeEndAtAndEndedAfterEndAt);
        }
    }

    @Nested
    class FindByDateRangeOfOneDay {
        Instant startTime = NOW;
        EventCategory category = category15MinutesA;
        EventCategory otherCategory = category15MinutesB;
        Instant endTime = startTime.plus(category.getEventDuration(), ChronoUnit.DAYS);
        Event eventStartedAtStartTime,
                eventStartedAfterStartTime,
                eventStartedAtEndTime,
                eventEndedBeforeStartTime,
                eventStartedAfterEndTime,
                eventStartedAtStartTime_other,
                eventStartedAfterStartTime_other,
                eventStartedAtEndTime_other,
                eventEndedBeforeStartTime_other,
                eventStartedAfterEndTime_other;

        @BeforeEach
        void init() {
            eventStartedAtStartTime = createEventAndSave(category, startTime, 1);
            eventStartedAfterStartTime = createEventAndSave(category, startTime.plus(1, ChronoUnit.MINUTES), 2);
            eventStartedAtEndTime = createEventAndSave(category, endTime);
            eventEndedBeforeStartTime = createEventAndSave(category, startTime.minus(1, ChronoUnit.MINUTES));
            eventStartedAfterEndTime = createEventAndSave(category, endTime.plus(1, ChronoUnit.MINUTES));

            eventStartedAtStartTime_other = createEventAndSave(otherCategory, startTime, 1);
            eventStartedAfterStartTime_other = createEventAndSave(otherCategory, startTime.plus(1, ChronoUnit.MINUTES), 2);
            eventStartedAtEndTime_other = createEventAndSave(otherCategory, endTime);
            eventEndedBeforeStartTime_other = createEventAndSave(otherCategory, startTime.minus(1, ChronoUnit.MINUTES));
            eventStartedAfterEndTime_other = createEventAndSave(otherCategory, endTime.plus(1, ChronoUnit.MINUTES));
        }

        @Test
        void returnEventsInACategory() {
            List<Event> events = eventRepository.findByDateRangeOfOneDay(startTime, List.of(category.getId()), null);
            assertThat(events).containsExactlyInAnyOrder(eventStartedAtStartTime, eventStartedAfterStartTime);
        }

        @Test
        void returnEventsInMultipleCategories() {
            List<Event> events = eventRepository.findByDateRangeOfOneDay(startTime, List.of(category.getId(), otherCategory.getId()), null);
            assertThat(events).containsExactlyInAnyOrder(eventStartedAtStartTime, eventStartedAfterStartTime, eventStartedAtStartTime_other, eventStartedAfterStartTime_other);
        }

        @Test
        void returnEventsInACategoryForAUser() {
            List<Event> events = eventRepository.findByDateRangeOfOneDay(startTime, List.of(category.getId()), 1);
            assertThat(events).containsExactlyInAnyOrder(eventStartedAtStartTime);
        }

        @Test
        void returnAllEvents() {
            List<Event> events = eventRepository.findByDateRangeOfOneDay(startTime, null, null);
            assertThat(events).containsExactlyInAnyOrder(eventStartedAtStartTime, eventStartedAfterStartTime, eventStartedAtStartTime_other, eventStartedAfterStartTime_other);
        }
    }

    private Event createEventAndSave(EventCategory category, Instant startAt) {
        return createEventAndSave(category, startAt, 0);
    }

    private Event createEvent(EventCategory category, Instant startAt) {
        return createEvent(category, startAt, 0);
    }

    private Event createEventAndSave(EventCategory category, Instant startAt, int userId) {
        Event event = createEvent(category, startAt, userId);
        saveAndRefresh(event);
        return event;
    }

    private Event createEvent(EventCategory category, Instant startAt, int userId) {
        Event event = new Event(category, "event name", "user@email.com", startAt, "event notes");
        User proxy = userRepository.getById(userId); // kinda hacky, but we don't care about the user for now
        event.setUser(proxy);
        return event;
    }

    private void saveAndRefresh(Event event) {
        eventRepository.save(event);
        entityManager.refresh(event);
    }

    private void saveAndRefresh(Collection<Event> events) {
        eventRepository.saveAll(events);
        events.forEach(entityManager::refresh);
    }
}

