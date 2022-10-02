package int221.oasip.backendus3.repository;

import int221.oasip.backendus3.entities.Event;
import int221.oasip.backendus3.entities.EventCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase
class EventRepositoryTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2022-05-05T08:00:00Z"), ZoneId.systemDefault());
    @Autowired
    EventCategoryRepository categoryRepository;
    @Autowired
    private EventRepository eventRepository;

    private EventCategory category15MinutesA;
    private EventCategory category15MinutesB;

    @BeforeEach
    void setUp() {
        category15MinutesA = categoryRepository.save(new EventCategory("15 minutes A", "category description", 15));
        category15MinutesB = categoryRepository.save(new EventCategory("15 minutes B", "category description", 15));
    }

    @Test
    void findUpcomingAndOngoingEvents_shouldReturnUpcomingAndOngoingEvents_whenNoCategoryIdIsProvided() {
        Instant startAt = FIXED_CLOCK.instant();
        Event ongoingEvent1 = createEvent(category15MinutesA, startAt);
        Event ongoingEvent2 = createEvent(category15MinutesB, startAt.minusSeconds(1));
        Event upcomingEvent1 = createEvent(category15MinutesA, startAt.plusSeconds(1));
        Event upcomingEvent2 = createEvent(category15MinutesB, startAt.plus(15, ChronoUnit.DAYS));
        Event pastEvent1 = createEvent(category15MinutesA, startAt.minus(15, ChronoUnit.DAYS));
        eventRepository.save(ongoingEvent1);
        eventRepository.save(ongoingEvent2);
        eventRepository.save(upcomingEvent1);
        eventRepository.save(upcomingEvent2);
        eventRepository.save(pastEvent1);

        List<Event> events = eventRepository.findUpcomingAndOngoingEvents(startAt, null);

        assertEquals(4, events.size());
    }

    @Test
    void findUpcomingAndOngoingEvents_shouldReturnUpcomingAndOngoingEventsInTheSameCategory_whenCategoryIdIsProvided() {
        Instant startAt = FIXED_CLOCK.instant();
        EventCategory targetCategory = category15MinutesA;
        Event expectedOngoingEvent1 = createEvent(targetCategory, startAt);
        Event ongoingEvent2 = createEvent(category15MinutesB, startAt.minusSeconds(1));
        Event expectedUpcomingEvent1 = createEvent(targetCategory, startAt.plusSeconds(1));
        Event upcomingEvent2 = createEvent(category15MinutesB, startAt.plus(15, ChronoUnit.DAYS));
        Event pastEvent1 = createEvent(targetCategory, startAt.minus(15, ChronoUnit.DAYS));
        eventRepository.save(expectedOngoingEvent1);
        eventRepository.save(ongoingEvent2);
        eventRepository.save(expectedUpcomingEvent1);
        eventRepository.save(upcomingEvent2);
        eventRepository.save(pastEvent1);

        List<Event> events = eventRepository.findUpcomingAndOngoingEvents(startAt, targetCategory.getId());

        assertEquals(2, events.size());
    }

    @Test
    void findUpcomingAndOngoingEvents_shouldReturnEventsThatStartAtTheSameTimeAsTheStartAt() {
        Instant startAt = FIXED_CLOCK.instant();
        Event eventStartingAtStartAt = createEvent(category15MinutesA, startAt);
        eventRepository.save(eventStartingAtStartAt);

        List<Event> events = eventRepository.findUpcomingAndOngoingEvents(startAt, null);

        assertEquals(1, events.size());
    }


    @Test
    void findPastEvents_shouldReturnPastEvents_whenNoCategoryIdIsProvided() {
        Instant startAt = FIXED_CLOCK.instant();
        Instant fifteenMinutesAgo = startAt.minus(15, ChronoUnit.MINUTES);
        Event pastEvent1 = createEvent(category15MinutesA, fifteenMinutesAgo);
        Event pastEvent2 = createEvent(category15MinutesB, fifteenMinutesAgo);
        Event ongoingEvent1 = createEvent(category15MinutesA, fifteenMinutesAgo.plusSeconds(1));
        eventRepository.save(pastEvent1);
        eventRepository.save(pastEvent2);
        eventRepository.save(ongoingEvent1);

        List<Event> events = eventRepository.findPastEvents(startAt, null);

        assertEquals(2, events.size());
    }

    @Test
    void findPastEvents_shouldReturnPastEventsInTheSameCategory_whenCategoryIdIsProvided() {
        Instant startAt = FIXED_CLOCK.instant();
        EventCategory targetCategory = category15MinutesA;
        Instant fifteenMinutesAgo = startAt.minus(15, ChronoUnit.MINUTES);
        Event pastEvent1 = createEvent(targetCategory, fifteenMinutesAgo);
        Event pastEvent2 = createEvent(category15MinutesB, fifteenMinutesAgo);
        Event ongoingEvent1 = createEvent(targetCategory, fifteenMinutesAgo.plusSeconds(1));
        eventRepository.save(pastEvent1);
        eventRepository.save(pastEvent2);
        eventRepository.save(ongoingEvent1);

        List<Event> events = eventRepository.findPastEvents(startAt, targetCategory.getId());

        assertEquals(1, events.size());
    }

    @Test
    void findPastEvents_shouldReturnEventsThatEndedAtTheSameTimeAsTheStartAt() {
        Instant startAt = FIXED_CLOCK.instant();
        Instant fifteenMinutesAgo = startAt.minus(15, ChronoUnit.MINUTES);
        Event eventEndingAtStartAt = createEvent(category15MinutesA, fifteenMinutesAgo);
        eventRepository.save(eventEndingAtStartAt);

        List<Event> events = eventRepository.findPastEvents(startAt, null);

        assertEquals(1, events.size());
    }


    @Test
    void findOverlapEventsByCategoryId_shouldReturnOverlapEventsInTheSameCategory() {
        Instant startAt = FIXED_CLOCK.instant();
        Instant endAt = startAt.plus(15, ChronoUnit.MINUTES);
        Event eventStartingBeforeEndAt = createEvent(category15MinutesA, endAt.minusSeconds(1));
        Event eventEndingAfterStartAt = createEvent(category15MinutesA, startAt.minus(15, ChronoUnit.MINUTES).plusSeconds(1));
        Event differentCategoryEventStartingBeforeEndAt = createEvent(category15MinutesB, endAt.minusSeconds(1));
        eventRepository.save(eventEndingAfterStartAt);
        eventRepository.save(eventStartingBeforeEndAt);
        eventRepository.save(differentCategoryEventStartingBeforeEndAt);

        List<Event> events = eventRepository.findOverlapEventsByCategoryId(startAt, endAt, category15MinutesA.getId(), null);

        assertEquals(2, events.size());
    }

    @Test
    void findOverlapEventsByCategoryId_shouldNotReturnNonOverlappingEventsInTheSameCategory() {
        Instant startAt = FIXED_CLOCK.instant();
        Instant endAt = startAt.plus(15, ChronoUnit.MINUTES);
        Event eventStartingAtStartAt = createEvent(category15MinutesA, endAt);
        Event eventEndingAtStartAt = createEvent(category15MinutesA, startAt.minus(15, ChronoUnit.MINUTES));
        Event differentCategoryEventStartingAtStartAt = createEvent(category15MinutesB, endAt);
        eventRepository.save(eventEndingAtStartAt);
        eventRepository.save(eventStartingAtStartAt);
        eventRepository.save(differentCategoryEventStartingAtStartAt);

        List<Event> events = eventRepository.findOverlapEventsByCategoryId(startAt, endAt, category15MinutesA.getId(), null);

        assertEquals(0, events.size());
    }

    private Event createEvent(EventCategory category, Instant startAt) {
        return new Event(category, "event name", "user@email.com", startAt, "event notes");
    }
}