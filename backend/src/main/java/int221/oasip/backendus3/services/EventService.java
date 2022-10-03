package int221.oasip.backendus3.services;

import int221.oasip.backendus3.dtos.CreateEventRequest;
import int221.oasip.backendus3.dtos.EditEventRequest;
import int221.oasip.backendus3.dtos.EventResponse;
import int221.oasip.backendus3.entities.Event;
import int221.oasip.backendus3.entities.EventCategory;
import int221.oasip.backendus3.entities.User;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.EventOverlapException;
import int221.oasip.backendus3.repository.EventCategoryRepository;
import int221.oasip.backendus3.repository.EventRepository;
import int221.oasip.backendus3.repository.UserRepository;
import int221.oasip.backendus3.utils.ModelMapperUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class EventService {
    private EventRepository repository;
    private ModelMapper modelMapper;
    private ModelMapperUtils modelMapperUtils;
    private EventCategoryRepository categoryRepository;
    private UserRepository userRepository;

    public EventResponse getEvent(Integer id) {
        Event event = repository.findById(id).orElse(null);

        if (event == null) {
            return null;
        }

        return modelMapper.map(event, EventResponse.class);
    }

    public EventResponse create(CreateEventRequest newEvent) {
        Event e = new Event();
        EventCategory category = categoryRepository.findById(newEvent.getEventCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Event category with id " + newEvent.getEventCategoryId() + " not found"));
        User user = userRepository.findByEmail(newEvent.getBookingEmail())
                .orElseThrow(() -> new EntityNotFoundException("User with email " + newEvent.getBookingEmail() + " not found"));

        e.setBookingName(newEvent.getBookingName().strip());
        e.setBookingEmail(newEvent.getBookingEmail().strip());
        e.setEventStartTime(Instant.from(newEvent.getEventStartTime()));
        e.setEventCategory(category);
        e.setEventDuration(category.getEventDuration());
        e.setUser(user);
        if (newEvent.getEventNotes() != null) {
            e.setEventNotes(newEvent.getEventNotes().strip());
        }

        Instant startTime = e.getEventStartTime();
        Instant endTime = startTime.plus(e.getEventDuration(), ChronoUnit.MINUTES);

        List<Event> overlapEvents = repository.findOverlapEventsByCategoryId(startTime, endTime, e.getEventCategory().getId(), null);

        if (overlapEvents.size() > 0) {
            throw new EventOverlapException();
        }

        e.setId(null);

        return modelMapper.map(repository.saveAndFlush(e), EventResponse.class);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public EventResponse update(Integer id, EditEventRequest editEvent) {
        Event event = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Event with id " + id + " not found"));

        if (editEvent.getEventNotes() != null) {
            event.setEventNotes(editEvent.getEventNotes().strip());
        }

        if (editEvent.getEventStartTime() != null) {
            Instant startTime = Instant.from(editEvent.getEventStartTime());
            Instant endTime = startTime.plus(event.getEventDuration(), ChronoUnit.MINUTES);
            Integer categoryId = event.getEventCategory().getId();
            Integer eventId = event.getId();

            List<Event> overlapEvents = repository.findOverlapEventsByCategoryId(startTime, endTime, categoryId, eventId);

            if (overlapEvents.size() > 0) {
                throw new EventOverlapException();
            } else {
                event.setEventStartTime(startTime);
            }
        }

        return modelMapper.map(repository.saveAndFlush(event), EventResponse.class);
    }

    /**
     * if {@code categoryId} is specified, it will be used in all queries, otherwise all categories is assumed
     * <br />
     * if {@code type} is {@link EventTimeType#DAY}, {@code startAt} must be specified, otherwise, {@link IllegalArgumentException} will be thrown
     * <br />
     * if {@code type} is not {@link EventTimeType#DAY}}, {@code startAt} is ignored and the current time is used instead
     * <br />
     * if {@code type} is not specified, it will be set to all
     * <br />
     * if {@code type} is specified, it must be parsable to {@link EventTimeType}, otherwise {@link IllegalArgumentException} will be thrown
     * <br />
     * if {@code userEmail} is specified, it will be used to find events that the user has booked
     * <br />
     * if {@code isAdmin} is {@code true}, {@code userEmail} is ignored
     *
     * @param options options
     * @return List of events based on the options provided
     * @throws IllegalArgumentException if the {@code type} is {@link EventTimeType#DAY} and {@code startAt} is null
     */
    public List<EventResponse> getEvents(GetEventsOptions options) {
        EventTimeType type = EventTimeType.fromString(options.getType());
        Instant startAt = options.getStartAt();
        Integer categoryId = options.getCategoryId();
        Instant now = Instant.now();

        User user = null;
        if (!options.isAdmin()) {
            user = userRepository.findByEmail(options.getUserEmail())
                    .orElseThrow(() -> new EntityNotFoundException("User with email " + options.getUserEmail() + " not found"));
        }
        Integer userId = user != null ? user.getId() : null;

        List<Event> events;
        if (EventTimeType.DAY.equals(type)) {
            if (startAt == null) {
                throw new IllegalArgumentException("startAt cannot be null for type " + EventTimeType.DAY);
            }
            events = repository.findByDateRangeOfOneDay(startAt, categoryId, userId);
        } else if (EventTimeType.UPCOMING.equals(type)) {
            events = repository.findUpcomingAndOngoingEvents(now, categoryId, userId);
        } else if (EventTimeType.PAST.equals(type)) {
            events = repository.findPastEvents(now, categoryId, userId);
        } else if (type != null) {
            throw new IllegalArgumentException("type " + type + " is not supported");
        } else if (categoryId != null) {
            events = repository.findByEventCategory_IdAndUser_Id(categoryId, userId);
        } else if (userId != null) {
            events = repository.findByUser_Id(userId);
        } else {
            events = repository.findAll();
        }

        return modelMapperUtils.mapList(events, EventResponse.class);
    }

    public List<EventResponse> getEventsForLecturer(String email) {
        return null;
    }

    public static enum EventTimeType {
        UPCOMING, PAST, DAY;

        public static EventTimeType fromString(String type) {
            if (type == null) {
                return null;
            }

            try {
                return EventTimeType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @Builder(builderClassName = "Builder")
    @Getter
    public static class GetEventsOptions {
        private Instant startAt;
        private Integer categoryId;
        private String type;
        private String userEmail;
        private boolean isAdmin;
    }
}