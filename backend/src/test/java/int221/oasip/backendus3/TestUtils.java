package int221.oasip.backendus3;

import int221.oasip.backendus3.entities.Event;
import int221.oasip.backendus3.entities.EventCategory;
import int221.oasip.backendus3.entities.User;
import int221.oasip.backendus3.repository.EventRepository;
import int221.oasip.backendus3.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;

@Component
public class TestUtils {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    public Event createEventAndSave(EventCategory category, Instant startAt) {
        return createEventAndSave(category, startAt, 0);
    }

    private Event createEvent(EventCategory category, Instant startAt) {
        return createEvent(category, startAt, 0);
    }

    public Event createEventAndSave(EventCategory category, Instant startAt, int userId) {
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
