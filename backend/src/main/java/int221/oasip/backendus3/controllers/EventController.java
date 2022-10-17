package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.dtos.CreateEventMultipartRequest;
import int221.oasip.backendus3.dtos.EditEventMultipartRequest;
import int221.oasip.backendus3.dtos.EditEventRequest;
import int221.oasip.backendus3.dtos.EventResponse;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.EventOverlapException;
import int221.oasip.backendus3.exceptions.FieldNotValidException;
import int221.oasip.backendus3.services.EventService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public EventResponse create(@Valid CreateEventMultipartRequest newEvent, Authentication authentication) {
        boolean isGuest = authentication == null;
        boolean isAdmin = authentication != null && isAdmin(authentication);

        if (!isGuest && !isAdmin && !authentication.getName().equals(newEvent.getBookingEmail())) {
            // if the user is not a guest, admin or the owner of the event, then they are not allowed to create the event for someone else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email in request body does not match the authenticated user");
        }

        try {
            return service.create(newEvent, isGuest, isAdmin);
        } catch (EventOverlapException e) {
            throw new FieldNotValidException("eventStartTime", e.getMessage());
        } catch (EntityNotFoundException e) {
            // category not found or user not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email");
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
    public EventResponse update(@PathVariable Integer id, @Valid EditEventMultipartRequest editEvent, Authentication authentication) {
        if (editEvent.getEventStartTime() == null && editEvent.getEventNotes() == null
                && editEvent.getFile() == null && !editEvent.getFile().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one of eventStartTime, eventNotes, or file must be provided");
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
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update file");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // with optional query parameter to only fetch the file name without the file content
    @GetMapping("/files/{uuid}")
    public ResponseEntity<?> getFile(
            @PathVariable String uuid,
            @RequestParam(required = false) Boolean noContent
    ) throws IOException {
        File file = service.getFileByBucketUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        Path path = Paths.get(file.getAbsolutePath());
        Resource resource = new ByteArrayResource(Files.readAllBytes(path));
        String contentType = Files.probeContentType(path);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getName());

        ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok()
                .headers(headers)
                .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType));

        if (noContent == null || !noContent) {
            return bodyBuilder.body(resource);
        } else {
            return bodyBuilder.body(file.getName());
        }
    }
}
