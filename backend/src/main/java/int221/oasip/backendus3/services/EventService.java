package int221.oasip.backendus3.services;

import int221.oasip.backendus3.dtos.*;
import int221.oasip.backendus3.entities.Event;
import int221.oasip.backendus3.entities.EventCategory;
import int221.oasip.backendus3.entities.File;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.EventOverlapException;
import int221.oasip.backendus3.exceptions.ForbiddenException;
import int221.oasip.backendus3.repository.EventCategoryOwnerRepository;
import int221.oasip.backendus3.repository.EventCategoryRepository;
import int221.oasip.backendus3.repository.EventRepository;
import int221.oasip.backendus3.services.auth.AuthStatus;
import int221.oasip.backendus3.services.auth.AuthUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repository;
    private final ModelMapper modelMapper;
    private final EventCategoryRepository categoryRepository;
    private final EventCategoryOwnerRepository categoryOwnerRepository;
    private final FileService fileService;
    private final MailService mailService;
    private final AuthUtil authUtil;
    private final ForbiddenException COMMON_FORBIDDEN_EXCEPTION = new ForbiddenException("User with this email is not allowed to access this resource");

    public EventResponse getEvent(Integer id) {
        Event event = getEventIfAuthorized(id);
        return mapEventToEventResponse(event);
    }

    public EventResponse create(CreateEventMultipartRequest newEvent) throws MessagingException, IOException {
        Event event = new Event();
        event.setBookingName(newEvent.getBookingName().strip());
        event.setBookingEmail(newEvent.getBookingEmail().strip());
        event.setEventStartTime(Instant.from(newEvent.getEventStartTime()));
        if (newEvent.getEventNotes() != null) {
            event.setEventNotes(newEvent.getEventNotes().strip());
        }

        assertEventOwner(event);

        Integer categoryId = newEvent.getEventCategoryId();
        EventCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Event category with id " + categoryId + " not found"));
        event.setEventCategory(category);

        List<Event> overlapEvents = getOverlapEvents(event);
        if (overlapEvents.size() > 0) {
            throw new EventOverlapException();
        }

        setFileForEventIfExist(event, newEvent.getFile());
        mailService.sendmail(event);

        return mapEventToEventResponse(saveAndRefresh(event));
    }

    private Event saveAndRefresh(Event event) {
        Event savedEvent = repository.saveAndFlush(event);
        return repository.findById(savedEvent.getId()).orElseThrow();
    }

    private EventResponse mapEventToEventResponse(Event event) {
        EventResponse eventResponse = modelMapper.map(event, EventResponse.class);
        List<FileInfoResponse> files = event.getFiles().stream().map(file -> {
            FileInfoResponse info = new FileInfoResponse();
            info.setBucketId(file.getBucketId());
            info.setName(file.getName());
            info.setType(file.getType());
            return info;
        }).collect(Collectors.toList());

        eventResponse.setFiles(files);

        return eventResponse;
    }

    private List<EventResponse> mapEventsToEventResponses(List<Event> events) {
        return events.stream().map(this::mapEventToEventResponse).collect(Collectors.toList());
    }

    private void setFileForEventIfExist(Event event, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String bucketId = UUID.randomUUID().toString();
            fileService.uploadFile(bucketId, file);
            File fileEntity = new File();
            fileEntity.setBucketId(bucketId);
            fileEntity.setName(file.getOriginalFilename());
            fileEntity.setType(file.getContentType());
            fileEntity.setEvent(event);
            fileService.create(fileEntity);
            event.getFiles().add(fileEntity);
            System.out.println("Created file " + fileEntity.getId() + " bucketId " + fileEntity.getBucketId());
        }
    }

    private List<Event> getOverlapEvents(Event event) {
        Instant startTime = event.getEventStartTime();
        Instant endTime = startTime.plus(event.getEventDuration(), ChronoUnit.MINUTES);
        return repository.findOverlapEventsByCategoryId(startTime, endTime, event.getEventCategory().getId(), null);
    }

    private void assertEventOwner(Event event) {
        AuthStatus authStatus = authUtil.getAuthStatus();
        Authentication authentication = authStatus.getAuthentication();

        if (!authStatus.isGuest && !authStatus.isAdmin && authentication != null && !authStatus.getEmail().equals(event.getBookingEmail())) {
            // if the user is not a guest, admin or the owner of the event, then they are not allowed to create the event for someone else
            throw new ForbiddenException("User is not allowed to create event on behalf of another user");
        }
    }

    public void delete(Integer id) {
        Event event = getEventIfAuthorized(id);
        removeFileFromEvent(event);
        repository.deleteById(id);
    }

    private Event getEventIfAuthorized(Integer id) {
        Event event = repository.findById(id).orElse(null);
        if (event == null) {
            throw new EntityNotFoundException("Event with id " + id + " not found");
        }

        AuthStatus authStatus = authUtil.getAuthStatus();
        String email = authStatus.getEmail();
        if (!authStatus.isAdmin && !event.getBookingEmail().equals(email)) {
            throw new ForbiddenException("You are not allowed to access this event");
        }

        return event;
    }

    public EventResponse update(Integer id, EditEventMultipartRequest editEvent) throws IOException {
        Event event = getEventIfAuthorized(id);

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

        updateFileForEvent(event, editEvent.getFile());

        return mapEventToEventResponse(saveAndRefresh(event));
    }

    private void updateFileForEvent(Event event, MultipartFile newFile) throws IOException {
        if (newFile != null) { // if there is a new file
            removeFileFromEvent(event); // remove the old file regardless of whether the new file is empty or not

            if (!newFile.isEmpty()) { // if the new file is not empty, then create a new file
                setFileForEventIfExist(event, newFile);
            }
        }

    }

    private void removeFileFromEvent(Event event) {
        List<File> existingFiles = event.getFiles();
        existingFiles.forEach(file -> {
            try {
                fileService.deleteFile(file.getBucketId(), file.getName());
            } catch (Exception ignored) {
            }
            System.out.println("Deleted file " + file.getName() + " (" + file.getId() + ") from bucket " + file.getBucketId());
        });

        event.getFiles().clear();
    }

    public List<EventResponse> getEventsNew(GetEventsOptions options) {
        EventTimeType type = options.getType();
        Instant startAt = options.getStartAt();
        Collection<Integer> categoryIds = options.getCategoryIds();

        if (EventTimeType.DAY.equals(type)) {
            if (startAt == null) {
                throw new IllegalArgumentException("startAt cannot be null for type " + EventTimeType.DAY);
            }
            return getEventsByDay(startAt, categoryIds);
        }

        Instant now = Instant.now();
        if (EventTimeType.UPCOMING.equals(type)) {
            return getUpcomingEvents(now, categoryIds);
        }
        if (EventTimeType.PAST.equals(type)) {
            return getPastEvents(now, categoryIds);
        }
        if (categoryIds != null) {
            return getEventsByCategory(categoryIds);
        }

        return getAllEvents();
    }

    public List<EventResponse> getEventsByDay(Instant startAt, Collection<Integer> categoryIds) {
        UserAwareFindEventsParameters parameters = new UserAwareFindEventsParameters(categoryIds);
        List<Event> events = repository.findByDateRangeOfOneDay(startAt, parameters.categoryIds, parameters.email);
        return mapEventsToEventResponses(events);
    }

    public List<EventResponse> getPastEvents(Instant startAt, Collection<Integer> categoryIds) {
        UserAwareFindEventsParameters parameters = new UserAwareFindEventsParameters(categoryIds);
        List<Event> events = repository.findPastEvents(startAt, parameters.categoryIds, parameters.email);
        return mapEventsToEventResponses(events);
    }

    public List<EventResponse> getUpcomingEvents(Instant startAt, Collection<Integer> categoryIds) {
        UserAwareFindEventsParameters parameters = new UserAwareFindEventsParameters(categoryIds);
        List<Event> events = repository.findUpcomingAndOngoingEvents(startAt, parameters.categoryIds, parameters.email);
        return mapEventsToEventResponses(events);
    }

    public List<EventResponse> getEventsByCategory(Collection<Integer> categoryIds) {
        List<Event> events;
        AuthStatus authStatus = authUtil.getAuthStatus();
        String email = authStatus.getEmail();

        if (authStatus.isAdmin) {
            events = repository.findByEventCategory_IdIn(categoryIds);
        } else if (authStatus.isLecturer) {
            Set<Integer> filteredCategoryIds = getFilteredCategoryIdsForLecturer(email, categoryIds);
            events = repository.findByEventCategory_IdIn(filteredCategoryIds);
        } else if (authStatus.isStudent) {
            events = repository.findByEventCategory_IdInAndBookingEmail(categoryIds, email);
        } else {
            throw COMMON_FORBIDDEN_EXCEPTION;
        }

        return mapEventsToEventResponses(events);
    }

    public List<EventResponse> getAllEvents() {
        List<Event> events;
        AuthStatus authStatus = authUtil.getAuthStatus();
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

        return mapEventsToEventResponses(events);
    }

    private Set<Integer> getFilteredCategoryIdsForLecturer(String email, @Nullable Collection<Integer> untrustedCategoryIds) {
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

    @Getter
    public static class GetEventsOptions {
        @Nullable
        protected final EventTimeType type;
        @Nullable
        protected final Instant startAt;
        @Nullable
        protected final Collection<Integer> categoryIds;

        /**
         * Create a {@link GetEventsOptions} object based on the provided parameters
         *
         * @param type        the type of events to return, if null, all events is assumed, otherwise it must be parsable to {@link EventTimeType}
         * @param startAt     the start time, required not null if {@code type} is {@link EventTimeType#DAY}, otherwise ignored
         * @param categoryIds the category ids to filter, if null, all categories is assumed
         * @throws IllegalArgumentException if the {@code type} is {@link EventTimeType#DAY} and {@code startAt} is null or the {@code type} is not parsable to {@link EventTimeType}
         */
        public GetEventsOptions(@Nullable String type, @Nullable Instant startAt, @Nullable Collection<Integer> categoryIds) {
            this.type = EventTimeType.fromString(type);
            if (EventTimeType.DAY.equals(this.type)) {
                if (startAt == null) {
                    throw new IllegalArgumentException("startAt cannot be null for type " + EventTimeType.DAY);
                }
            }

            this.categoryIds = categoryIds;
            this.startAt = startAt;
        }
    }

    class UserAwareFindEventsParameters {
        @Nullable
        final Collection<Integer> categoryIds;
        @Nullable
        final String email;

        public UserAwareFindEventsParameters(@Nullable Collection<Integer> categoryIds) {
            AuthStatus authStatus = authUtil.getAuthStatus();
            String email = authStatus.getEmail();

            if (authStatus.isLecturer) {
                this.categoryIds = getFilteredCategoryIdsForLecturer(email, categoryIds);
            } else {
                this.categoryIds = categoryIds;
            }

            if (authStatus.isStudent) {
                this.email = email;
            } else {
                this.email = null;
            }
        }
    }
}