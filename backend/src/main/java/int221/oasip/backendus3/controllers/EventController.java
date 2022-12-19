package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.dtos.CreateEventMultipartRequest;
import int221.oasip.backendus3.dtos.EditEventMultipartRequest;
import int221.oasip.backendus3.dtos.EventResponse;
import int221.oasip.backendus3.dtos.EventTimeSlotResponse;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.EventOverlapException;
import int221.oasip.backendus3.exceptions.FieldNotValidException;
import int221.oasip.backendus3.services.EventService;
import int221.oasip.backendus3.services.auth.AuthStatus;
import int221.oasip.backendus3.services.auth.AuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@AllArgsConstructor
public class EventController {
    private EventService service;
    private AuthUtil authUtils;

    @GetMapping("")
    public List<EventResponse> getEvents(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startAt,
            @RequestParam(required = false) String type
    ) {
        AuthStatus authStatus = authUtils.getAuthStatus();
        if (authStatus.isGuest) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to access this resource");
        }

        EventService.GetEventsOptions options = new EventService.GetEventsOptions(
                type,
                startAt == null ? null : startAt.toInstant(),
                categoryId == null ? null : List.of(categoryId));

        return service.getEventsNew(options);
    }

    @GetMapping("/allocatedTimeSlots")
    public List<EventTimeSlotResponse> getAllocatedTimeSlotsInCategoryOnDate(
            @RequestParam Integer categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startAt,
            @RequestParam(required = false) Integer excludeEventId
    ) {
        return service.getAllocatedTimeSlotsInCategoryOnDate(categoryId, startAt.toInstant(), excludeEventId);
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Integer id) {
        return service.getEvent(id);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("!hasRole('LECTURER')")
    public EventResponse create(@Valid CreateEventMultipartRequest newEvent) {
        try {
            return service.create(newEvent);
        } catch (EventOverlapException e) {
            throw new FieldNotValidException("eventStartTime", e.getMessage());
        } catch (EntityNotFoundException e) {
            // category not found or user not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MessagingException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("!hasRole('LECTURER')")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("!hasRole('LECTURER')")
    public EventResponse update(@PathVariable Integer id, @Valid EditEventMultipartRequest editEvent) {
        if (editEvent.getEventStartTime() == null && editEvent.getEventNotes() == null && editEvent.getFile() == null) {
            System.out.println("No fields to update");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one of eventStartTime, eventNotes, or file must be provided");
        }

        try {
            return service.update(id, editEvent);
        } catch (EventOverlapException e) {
            throw new FieldNotValidException("eventStartTime", e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update file");
        }
    }
}
