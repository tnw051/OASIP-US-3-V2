package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.dtos.CreateEventRequest;
import int221.oasip.backendus3.dtos.EditEventRequest;
import int221.oasip.backendus3.dtos.EventResponse;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.EventOverlapException;
import int221.oasip.backendus3.exceptions.FieldNotValidException;
import int221.oasip.backendus3.services.EventService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private EventService service;

    @GetMapping("")
    public List<EventResponse> getEvents(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startAt,
            @RequestParam(required = false) String type,
            Authentication authentication
    ) {
        EventService.GetEventsOptions options = EventService.GetEventsOptions.builder()
                .categoryId(categoryId)
                .startAt(startAt != null ? startAt.toInstant() : null)
                .type(type)
                .isAdmin(isAdmin(authentication))
                .userEmail(authentication.getName())
                .build();

        return service.getEvents(options);
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Integer id, Authentication authentication) {
        EventResponse event = service.getEvent(id);
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
    @PreAuthorize("!hasRole('LECTURER')")
    public EventResponse create(@Valid @RequestBody CreateEventRequest newEvent, Authentication authentication) {
        if (authentication != null) {
            String email = authentication.getName();
            if (!isAdmin(authentication) && !email.equals(newEvent.getBookingEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email in request body does not match the authenticated user");
            }
        }

        try {
            boolean isGuest = authentication == null;
            return service.create(newEvent, isGuest);
        } catch (EventOverlapException e) {
            throw new FieldNotValidException("eventStartTime", e.getMessage());
        } catch (EntityNotFoundException e) {
            // category not found or user not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("!hasRole('LECTURER')")
    public void delete(@PathVariable Integer id, Authentication authentication) {
        EventResponse event = service.getEvent(id);
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
    @PreAuthorize("!hasRole('LECTURER')")
    public EventResponse update(@PathVariable Integer id, @Valid @RequestBody EditEventRequest editEvent, Authentication authentication) {
        if (editEvent.getEventStartTime() == null && editEvent.getEventNotes() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one of eventStartTime or eventNotes must be provided");
        }

        EventResponse event = service.getEvent(id);
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

    @GetMapping("/test-lecturer")
    public List<EventResponse> getEvents(Authentication auth) {
        String email = auth.getName();
        System.out.println(email);
        return service.getEventsForLecturer(email);
    }

}
