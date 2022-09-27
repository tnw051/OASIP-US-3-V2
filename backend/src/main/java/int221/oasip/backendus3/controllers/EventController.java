package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.dtos.CreateEventRequestDTO;
import int221.oasip.backendus3.dtos.EditEventRequestDTO;
import int221.oasip.backendus3.dtos.EventResponseDTO;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.EventOverlapException;
import int221.oasip.backendus3.exceptions.FieldNotValidException;
import int221.oasip.backendus3.services.EventService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@AllArgsConstructor
public class EventController {
    public static final String DAY = "day";
    public static final String UPCOMING = "upcoming";
    public static final String PAST = "past";
    private EventService service;

    // TODO: refactor service methods and add email as a parameter
    @GetMapping("")
    public List<EventResponseDTO> getEvents(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startAt,
            @RequestParam(required = false) String type,
            Authentication authentication
    ) {
        if (categoryId != null) {
            if (DAY.equalsIgnoreCase(type) && startAt != null) {
                return service.getEventsOnDate(startAt.toInstant(), categoryId);
            } else if (UPCOMING.equalsIgnoreCase(type)) {
                return service.getUpcomingAndOngoingEvents(categoryId);
            } else if (PAST.equalsIgnoreCase(type)) {
                return service.getPastEvents(categoryId);
            } else {
                return service.getEventsInCategory(categoryId);
            }
        } else {
            if (DAY.equalsIgnoreCase(type) && startAt != null) {
                return service.getEventsOnDate(startAt.toInstant());
            } else if (UPCOMING.equalsIgnoreCase(type)) {
                return service.getUpcomingAndOngoingEvents();
            } else if (PAST.equalsIgnoreCase(type)) {
                return service.getPastEvents();
            } else {
                return service.getAll();
            }
        }
    }

    @GetMapping("/{id}")
    public EventResponseDTO getEventById(@PathVariable Integer id, Authentication authentication) {
        EventResponseDTO event = service.getEvent(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with id " + id + " not found");
        }

        String email = authentication.getName();
        if (!isAdmin(authentication) && !event.getBookingEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this event");
        }

        return event;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponseDTO create(@Valid @RequestBody CreateEventRequestDTO newEvent, Authentication authentication) {
        if (authentication != null) {
            String email = authentication.getName();
            if (!isAdmin(authentication) && !email.equals(newEvent.getBookingEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email in request body does not match the authenticated user");
            }
        }

        try {
            return service.create(newEvent);
        } catch (EventOverlapException e) {
            throw new FieldNotValidException("eventStartTime", e.getMessage());
        } catch (EntityNotFoundException e) {
            // category not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id, Authentication authentication) {
        EventResponseDTO event = service.getEvent(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with id " + id + " not found");
        }

        String email = authentication.getName();
        if (!isAdmin(authentication) && !event.getBookingEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this event");
        }

        service.delete(id);
    }

    @PatchMapping("/{id}")
    public EventResponseDTO update(@PathVariable Integer id, @Valid @RequestBody EditEventRequestDTO editEvent, Authentication authentication) {
        if (editEvent.getEventStartTime() == null && editEvent.getEventNotes() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one of eventStartTime or eventNotes must be provided");
        }

        EventResponseDTO event = service.getEvent(id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with id " + id + " not found");
        }

        String email = authentication.getName();
        if (!isAdmin(authentication) && !event.getBookingEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this event");
        }

        try {
            return service.update(id, editEvent);
        } catch (EventOverlapException e) {
            throw new FieldNotValidException("eventStartTime", e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
