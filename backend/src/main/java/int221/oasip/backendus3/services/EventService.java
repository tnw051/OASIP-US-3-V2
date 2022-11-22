package int221.oasip.backendus3.services;

import int221.oasip.backendus3.configs.AuthUtils;
import int221.oasip.backendus3.controllers.AuthStatus;
import int221.oasip.backendus3.dtos.CreateEventMultipartRequest;
import int221.oasip.backendus3.dtos.EditEventMultipartRequest;
import int221.oasip.backendus3.dtos.EventResponse;
import int221.oasip.backendus3.dtos.EventTimeSlotResponse;
import int221.oasip.backendus3.entities.Event;
import int221.oasip.backendus3.entities.EventCategory;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.EventOverlapException;
import int221.oasip.backendus3.exceptions.FieldNotValidException;
import int221.oasip.backendus3.exceptions.ForbiddenException;
import int221.oasip.backendus3.repository.EventCategoryOwnerRepository;
import int221.oasip.backendus3.repository.EventCategoryRepository;
import int221.oasip.backendus3.repository.EventRepository;
import int221.oasip.backendus3.utils.ModelMapperUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repository;
    private final ModelMapper modelMapper;
    private final ModelMapperUtils modelMapperUtils;
    private final EventCategoryRepository categoryRepository;
    private final EventCategoryOwnerRepository categoryOwnerRepository;
    private final FileService fileService;

    private final MailService mailService;
    private final AuthUtils authUtils;
    private final ForbiddenException COMMON_FORBIDDEN_EXCEPTION = new ForbiddenException("User with this email is not allowed to access this resource");

    public EventResponse getEvent(Integer id) {
        Event event = repository.findById(id).orElse(null);

        if (event == null) {
            return null;
        }

        return modelMapper.map(event, EventResponse.class);
    }

    public EventResponse create(CreateEventMultipartRequest newEvent) throws MessagingException, IOException {
        AuthStatus authStatus = authUtils.getAuthStatus();
        Authentication authentication = authStatus.getAuthentication();

        if (!authStatus.isGuest && !authStatus.isAdmin && authentication != null && !authStatus.getEmail().equals(newEvent.getBookingEmail())) {
            // if the user is not a guest, admin or the owner of the event, then they are not allowed to create the event for someone else
            throw new FieldNotValidException("bookingEmail", "Email in request body does not match the authenticated user");
        }

        Event e = new Event();
        EventCategory category = categoryRepository.findById(newEvent.getEventCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Event category with id " + newEvent.getEventCategoryId() + " not found"));

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

        if (newEvent.getFile() != null && !newEvent.getFile().isEmpty()) {
            String bucketUuid = fileService.uploadFile(newEvent.getFile());
            e.setBucketUuid(bucketUuid);
        }

        mailService.sendmail(e);

        return modelMapper.map(repository.saveAndFlush(e), EventResponse.class);
    }

    public void delete(Integer id) {
        Event event = repository.findById(id).orElse(null);
        if (event == null) {
            return;
        }

        fileService.deleteFileByBucketUuid(event.getBucketUuid());

        repository.deleteById(id);
    }

    public EventResponse update(Integer id, EditEventMultipartRequest editEvent) throws IOException {
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

        String bucketUuid = event.getBucketUuid();
        if (editEvent.getFile() != null) {
            // remove the old file
            if (editEvent.getFile().isEmpty()) {
                fileService.deleteFileByBucketUuid(bucketUuid);
                event.setBucketUuid(null);
            } else {
                // replace the old file with the new file
                if (bucketUuid != null) {
                    fileService.replaceFile(bucketUuid, editEvent.getFile());
                } else {
                    String newBucketUuid = fileService.uploadFile(editEvent.getFile());
                    event.setBucketUuid(newBucketUuid);
                }
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
    public List<EventResponse> getEventsNew(GetEventsOptions options) {
        EventTimeType type = EventTimeType.fromString(options.getType());

        Instant startAt = options.getStartAt();
        Integer categoryId = options.getCategoryId();
        AuthStatus authStatus = authUtils.getAuthStatus();

        List<Integer> categoryIds = categoryId == null ? null : List.of(categoryId);

        if (EventTimeType.DAY.equals(type)) {
            if (startAt == null) {
                throw new IllegalArgumentException("startAt cannot be null for type " + EventTimeType.DAY);
            }
            System.out.println("getEventsByDay");
            return getEventsByDay(authStatus, categoryIds, startAt);
        }

        if (EventTimeType.UPCOMING.equals(type)) {
            System.out.println("getUpcomingEvents");
            return getUpcomingEvents(authStatus, categoryIds);
        }

        if (EventTimeType.PAST.equals(type)) {
            System.out.println("getPastEvents");
            return getPastEvents(authStatus, categoryIds);
        }

        if (categoryId != null) {
            System.out.println("getEventsByCategory");
            return getEventsByCategory(authStatus, categoryIds);
        }

        System.out.println("getAllEvents");
        return getAllEvents(authStatus);
    }

    public List<EventResponse> getEventsByDay(AuthStatus authStatus, @Nullable List<Integer> categoryIds, Instant startAt) {
        List<Event> events;
        String email = authStatus.getEmail();

        if (authStatus.isAdmin) {
            events = repository.findByDateRangeOfOneDay(startAt, categoryIds, null);
        } else if (authStatus.isLecturer) {
            Set<Integer> filteredCategoryIds = getFilteredCategoryIdsForLecturer(email, categoryIds);
            events = repository.findByDateRangeOfOneDay(startAt, filteredCategoryIds, null);
        } else if (authStatus.isStudent) {
            events = repository.findByDateRangeOfOneDay(startAt, categoryIds, email);
        } else {
            throw COMMON_FORBIDDEN_EXCEPTION;
        }

        return modelMapperUtils.mapList(events, EventResponse.class);
    }

    public List<EventResponse> getUpcomingEvents(AuthStatus authStatus, @Nullable List<Integer> categoryIds) {
        List<Event> events;
        String email = authStatus.getEmail();

        if (authStatus.isAdmin) {
            events = repository.findUpcomingAndOngoingEvents(Instant.now(), categoryIds, null);
        } else if (authStatus.isLecturer) {
            Set<Integer> filteredCategoryIds = getFilteredCategoryIdsForLecturer(email, categoryIds);
            events = repository.findUpcomingAndOngoingEvents(Instant.now(), filteredCategoryIds, null);
        } else if (authStatus.isStudent) {
            events = repository.findUpcomingAndOngoingEvents(Instant.now(), categoryIds, email);
        } else {
            throw COMMON_FORBIDDEN_EXCEPTION;
        }

        return modelMapperUtils.mapList(events, EventResponse.class);
    }

    public List<EventResponse> getPastEvents(AuthStatus authStatus, @Nullable List<Integer> categoryIds) {
        List<Event> events;
        String email = authStatus.getEmail();

        if (authStatus.isAdmin) {
            events = repository.findPastEvents(Instant.now(), categoryIds, null);
        } else if (authStatus.isLecturer) {
            Set<Integer> filteredCategoryIds = getFilteredCategoryIdsForLecturer(email, categoryIds);
            events = repository.findPastEvents(Instant.now(), filteredCategoryIds, null);
        } else if (authStatus.isStudent) {
            events = repository.findPastEvents(Instant.now(), categoryIds, email);
        } else {
            throw COMMON_FORBIDDEN_EXCEPTION;
        }

        return modelMapperUtils.mapList(events, EventResponse.class);
    }

    @SuppressWarnings("unused")
    public List<EventResponse> getPastEventsExp(AuthStatus authStatus, @Nullable List<Integer> categoryIds) {
        String email = authStatus.getEmail();

        return processEvents(authStatus,
                () -> repository.findPastEvents(Instant.now(), categoryIds, null),
                () -> {
                    Set<Integer> filteredCategoryIds = getFilteredCategoryIdsForLecturer(email, categoryIds);
                    return repository.findPastEvents(Instant.now(), filteredCategoryIds, null);
                },
                () -> repository.findPastEvents(Instant.now(), categoryIds, email));
    }

    public List<EventResponse> getEventsByCategory(AuthStatus authStatus, @Nullable List<Integer> categoryIds) {
        List<Event> events;
        String email = authStatus.getEmail();

        if (authStatus.isAdmin) {
            events = repository.findByEventCategory_IdIn(categoryIds);
        } else if (authStatus.isLecturer) {
            Set<Integer> filteredCategoryIds = getFilteredCategoryIdsForLecturer(email, categoryIds);
            events = repository.findByEventCategory_IdIn(filteredCategoryIds);
        } else if (authStatus.isStudent) {
            events = repository.findByEventCategory_IdAndBookingEmail(categoryIds == null ? null : categoryIds.get(0), email);
        } else {
            throw COMMON_FORBIDDEN_EXCEPTION;
        }

        return modelMapperUtils.mapList(events, EventResponse.class);
    }

    // special case
    public List<EventResponse> getAllEvents(AuthStatus authStatus) {
        List<Event> events;
        String email = authStatus.getEmail();

        if (authStatus.isAdmin) {
            events = repository.findAll();
        } else if (authStatus.isLecturer) {
            Set<Integer> ownCategoryIds = getCategoryIdsForLecturer(email);
            events = repository.findByEventCategory_IdIn(ownCategoryIds);
        } else if (authStatus.isStudent) {
            events = repository.findByBookingEmail(email);
        } else {
            throw COMMON_FORBIDDEN_EXCEPTION;
        }

        return modelMapperUtils.mapList(events, EventResponse.class);
    }

//    private Set<EventCategory> getEventCategoriesForLecturer(User lecturer) {
//        return lecturer.getOwnCategories().stream().map(EventCategoryOwner::getEventCategory).collect(Collectors.toSet());
//    }

    private Set<Integer> getFilteredCategoryIdsForLecturer(String email, @Nullable List<Integer> untrustedCategoryIds) {
        Set<Integer> ownCategoryIds = getCategoryIdsForLecturer(email);
        if (untrustedCategoryIds != null) {
            ownCategoryIds.retainAll(untrustedCategoryIds);
        }
        return ownCategoryIds;
    }

    private Set<Integer> getCategoryIdsForLecturer(String email) {
        return this.categoryOwnerRepository.findByOwnerEmail(email).stream()
                .map(c -> c.getEventCategory().getId()).collect(Collectors.toSet());
    }

    //    delegate function that accept suppliers for different roles
    public List<EventResponse> processEvents(AuthStatus authStatus, Supplier<List<Event>> adminSupplier, Supplier<List<Event>> lecturerSupplier, Supplier<List<Event>> studentSupplier) {
        List<Event> events;

        if (authStatus.isAdmin) {
            events = adminSupplier.get();
        } else if (authStatus.isLecturer) {
            events = lecturerSupplier.get();
        } else if (authStatus.isStudent) {
            events = studentSupplier.get();
        } else {
            throw COMMON_FORBIDDEN_EXCEPTION;
        }

        return modelMapperUtils.mapList(events, EventResponse.class);
    }

    public List<EventTimeSlotResponse> getAllocatedTimeSlotsInCategoryOnDate(Integer categoryId, Instant startAt, Integer excludeEventId) {
        List<Event> events = repository.findByDateRangeOfOneDay(startAt, List.of(categoryId), null, excludeEventId);
        return events.stream()
                .map(event -> new EventTimeSlotResponse(event.getEventStartTime(), event.getEventDuration(), categoryId))
                .collect(Collectors.toList());
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
    @AllArgsConstructor
    public static class GetEventsOptions {
        @Nullable
        private Instant startAt;
        @Nullable
        private Integer categoryId;
        @Nullable
        private String type;
    }
}