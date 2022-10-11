package int221.oasip.backendus3.services;

import int221.oasip.backendus3.dtos.CreateEventRequest;
import int221.oasip.backendus3.dtos.EditEventRequest;
import int221.oasip.backendus3.dtos.EventResponse;
import int221.oasip.backendus3.entities.Event;
import int221.oasip.backendus3.entities.EventCategory;
import int221.oasip.backendus3.entities.Role;
import int221.oasip.backendus3.entities.User;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.EventOverlapException;
import int221.oasip.backendus3.exceptions.ForbiddenException;
import int221.oasip.backendus3.repository.EventCategoryRepository;
import int221.oasip.backendus3.repository.EventRepository;
import int221.oasip.backendus3.repository.UserRepository;
import int221.oasip.backendus3.utils.ModelMapperUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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

    public EventResponse create(CreateEventRequest newEvent, boolean isGuest) throws MessagingException, IOException {
        Event e = new Event();
        EventCategory category = categoryRepository.findById(newEvent.getEventCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Event category with id " + newEvent.getEventCategoryId() + " not found"));

        if (isGuest) {
            e.setUser(null);
        } else {
            User user = userRepository.findByEmail(newEvent.getBookingEmail())
                    .orElseThrow(() -> new EntityNotFoundException("User with email " + newEvent.getBookingEmail() + " not found"));
            e.setUser(user);
        }

        e.setBookingName(newEvent.getBookingName().strip());
        e.setBookingEmail(newEvent.getBookingEmail().strip());
        e.setEventStartTime(Instant.from(newEvent.getEventStartTime()));
        e.setEventCategory(category);
        e.setEventDuration(category.getEventDuration());
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
        sendmail(e);

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
     * <br />
     * if the user is a lecturer, events that the user owned will be returned, with the options of {@code categoryId} and {@code type} applied
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

        List<Integer> categoryIds = null;
        Integer userId = null;
        boolean isLecturer = false;

        if (!options.isAdmin()) {
            User user = userRepository.findByEmail(options.getUserEmail())
                    .orElseThrow(() -> new EntityNotFoundException("User with email " + options.getUserEmail() + " not found"));

            if (user.getRole().equals(Role.LECTURER)) {
                List<Integer> ownCategoryIds = user.getOwnCategories().stream().map(own -> own.getEventCategory().getId()).collect(Collectors.toList());
                if (categoryId != null && !ownCategoryIds.contains(categoryId)) {
                    throw new ForbiddenException("Lecturer with email " + options.getUserEmail() + " does not own category with id " + categoryId);
                }
                if (categoryId != null) {
                    categoryIds = List.of(categoryId);
                } else if (ownCategoryIds.size() > 0) {
                    categoryIds = ownCategoryIds;
                }
                isLecturer = true;

                System.out.println(user.getName() + " is a lecturer");
                System.out.println("Category IDs: " + categoryIds);
            } else {
                if (categoryId != null) {
                    categoryIds = List.of(categoryId);
                }
                userId = user.getId();
            }
        }

        List<Event> events;
        if (EventTimeType.DAY.equals(type)) {
            if (startAt == null) {
                throw new IllegalArgumentException("startAt cannot be null for type " + EventTimeType.DAY);
            }
            events = repository.findByDateRangeOfOneDay(startAt, categoryIds, userId);
        } else if (EventTimeType.UPCOMING.equals(type)) {
            events = repository.findUpcomingAndOngoingEvents(now, categoryIds, userId);
        } else if (EventTimeType.PAST.equals(type)) {
            events = repository.findPastEvents(now, categoryIds, userId);
        } else if (type != null) {
            throw new IllegalArgumentException("type " + type + " is not supported");
        } else if (categoryIds != null) {
            if (isLecturer) {
                events = repository.findByEventCategory_IdIn(categoryIds);
            } else {
                events = repository.findByEventCategory_IdAndUser_Id(categoryIds.get(0), userId);
            }
        } else if (userId != null) {
            events = repository.findByUser_Id(userId);
        } else if (options.isAdmin()) {
            events = repository.findAll();
        } else {
            return List.of();
        }

        return modelMapperUtils.mapList(events, EventResponse.class);
    }

    public enum EventTimeType {
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

    private void sendmail(Event event) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("oasip.us3.noreply@gmail.com", "hyyvvoygfnytkmgt");
            }
        });
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("E MMM dd, yyyy HH:mm").withZone(ZoneId.of("Asia/Bangkok"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm ").withZone(ZoneId.of("Asia/Bangkok"));
        Instant endTime = event.getEventStartTime().plusSeconds(event.getEventDuration() * 60);

        javax.mail.Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("oasip.us3.noreply@gmail.com", false));

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(event.getBookingEmail()));
        msg.setSubject("Your booking is complete.");
        String eventCategory = event.getEventCategory().getEventCategoryName();
        String eventNotes = event.getEventNotes();
        msg.setContent("Subject: [OASIP] " + eventCategory + " @ " + dateTimeFormatter.format(event.getEventStartTime()) + " - " + timeFormatter.format(endTime) + " (ICT)" +
                        "<br>Reply-to: noreply@intproj21.sit.kmutt.ac.th" +
                        "<br>Booking Name: " + event.getBookingName() +
                        "<br>Event Category: " + eventCategory +
                        "<br>When: " + dateTimeFormatter.format(event.getEventStartTime()) + " - " + timeFormatter.format(endTime) + " (ICT)" +
                        "<br>Event Notes: " + (eventNotes == null ? "" : eventNotes)

                , "text/html; charset=utf-8");
        msg.setSentDate(new Date());

        Transport.send(msg);
    }
}