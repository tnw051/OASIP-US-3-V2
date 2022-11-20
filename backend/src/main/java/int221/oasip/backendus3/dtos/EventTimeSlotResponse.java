package int221.oasip.backendus3.dtos;

import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
public class EventTimeSlotResponse {
    private final Instant eventStartTime;
    private final Instant eventEndTime;
    private final Integer eventDuration;
    private final Integer eventCategoryId;

    public EventTimeSlotResponse(Instant eventStartTime, Integer eventDuration, Integer eventCategoryId) {
        this.eventStartTime = eventStartTime;
        this.eventDuration = eventDuration;
        this.eventCategoryId = eventCategoryId;
        this.eventEndTime = eventStartTime.plus(eventDuration, ChronoUnit.MINUTES);
    }
}
