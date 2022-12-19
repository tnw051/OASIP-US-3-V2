package int221.oasip.backendus3.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;

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
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Nullable
    private List<FileInfoResponse> files;
}
