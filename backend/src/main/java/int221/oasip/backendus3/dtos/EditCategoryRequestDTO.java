package int221.oasip.backendus3.dtos;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Size;

@Getter
@Setter
public class EditCategoryRequestDTO {
    @Size(min = 1, max = 100, message = "Category name must be between {min} and {max} characters")
    private String eventCategoryName;

    @Size(max = 500, message = "Category descriptions must be less than 500 characters")
    private String eventCategoryDescription;

    @Range(min = 1, max = 480, message = "Category duration must be between {min} and {max} minutes")
    private Integer eventDuration;
}
