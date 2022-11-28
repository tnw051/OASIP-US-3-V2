package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.controllers.AuthStatus;
import int221.oasip.backendus3.entities.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ContextAwareEventCrudAuthorizer implements ContextAwareCrudAuthorizer<Event> {
    private final AuthUtils authUtils;

    @Override
    public void checkCreate(Event event) {
        AuthStatus authStatus = authUtils.getAuthStatus();

        if (!authStatus.isGuest && !authStatus.isAdmin && !authStatus.getEmail().equals(event.getBookingEmail())) {
            throw new AccessDeniedException("You are not allowed to create this event");
        }
    }

    @Override
    public void checkRead(Event event) {

    }

    @Override
    public void checkUpdate(Event event) {

    }

    @Override
    public void checkDelete(Event event) {

    }
}
