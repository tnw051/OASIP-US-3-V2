package int221.oasip.backendus3.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.validation.constraints.Future;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Getter
@Setter
public class EditEventRequest {
    @Future(message = "Start time must be in the future")
    @Nullable
    private OffsetDateTime eventStartTime;

    @Size(max = 500, message = "Event notes must be less than 500 characters")
    @Nullable
    private String eventNotes;
}
