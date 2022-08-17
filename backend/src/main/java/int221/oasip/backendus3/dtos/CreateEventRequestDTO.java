package int221.oasip.backendus3.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.time.OffsetDateTime;

@Getter
@Setter
public class CreateEventRequestDTO {
    @NotNull(message = "Event category ID must not be null")
    private Integer eventCategoryId;

    @NotBlank(message = "Booking name must not be blank")
    @Size(max = 100, message = "Booking name must be less than {max} characters")
    private String bookingName;

    @NotBlank(message = "Booking email must not be blank")
    @Size(max = 50, message = "Booking email must be less than {max} characters")
    @Email(message = "Booking email is invalid")
    private String bookingEmail;

    @NotNull(message = "Start time must not be null")
    @Future(message = "Start time must be in the future")
    private OffsetDateTime eventStartTime;

    @Size(max = 500, message = "Event notes must be less than {max} characters")
    private String eventNotes;
}