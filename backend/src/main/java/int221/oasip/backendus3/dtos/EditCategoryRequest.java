package int221.oasip.backendus3.dtos;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.annotation.Nullable;
import javax.validation.constraints.Size;

@Getter
@Setter
public class EditCategoryRequest {
    @Size(min = 1, max = 100, message = "Category name must be between {min} and {max} characters")
    @Nullable
    private String eventCategoryName;

    @Size(max = 500, message = "Category descriptions must be less than 500 characters")
    @Nullable
    private String eventCategoryDescription;

    @Range(min = 1, max = 480, message = "Category duration must be between {min} and {max} minutes")
    @Nullable
    private Integer eventDuration;
}
