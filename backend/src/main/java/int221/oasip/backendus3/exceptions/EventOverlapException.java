package int221.oasip.backendus3.exceptions;

public class EventOverlapException extends RuntimeException {
    public EventOverlapException() {
        super("Start time overlaps with other event(s)");
    }
}
