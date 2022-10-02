package int221.oasip.backendus3.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class EventResponse {
    private Integer id;
    private EventCategoryIdAndNameResponse eventCategory;
    private String bookingName;
    private String bookingEmail;
    private Instant eventStartTime;
    private Integer eventDuration;
    private String eventNotes;
}
