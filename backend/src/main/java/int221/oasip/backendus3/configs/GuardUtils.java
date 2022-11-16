package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.dtos.CreateEventMultipartRequest;
import int221.oasip.backendus3.dtos.EventResponse;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.EventOverlapException;
import int221.oasip.backendus3.exceptions.FieldNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.IOException;

@Component("guard")
public class GuardUtils {

//    @PostMapping("")
//    @ResponseStatus(HttpStatus.CREATED)
//    @PreAuthorize("!hasRole('LECTURER')")
//    public EventResponse create(@Valid CreateEventMultipartRequest newEvent, Authentication authentication) {
//        boolean isGuest = authentication == null;
//        boolean isAdmin = authentication != null && isAdmin(authentication);
//
//        if (!isGuest && !isAdmin && !authentication.getName().equals(newEvent.getBookingEmail())) {
//            // if the user is not a guest, admin or the owner of the event, then they are not allowed to create the event for someone else
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email in request body does not match the authenticated user");
//        }
//
//        try {
//            return service.create(newEvent, isGuest, isAdmin);
//        } catch (EventOverlapException e) {
//            throw new FieldNotValidException("eventStartTime", e.getMessage());
//        } catch (EntityNotFoundException e) {
//            // category not found or user not found
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
//        } catch (MessagingException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email");
//        } catch (IOException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email");
//        }
//    }

    // extract the avove method
    public boolean checkAdmin() {
        return getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
