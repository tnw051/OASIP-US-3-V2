package int221.oasip.backendus3.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponse {
    private Integer id;
    private String eventCategoryName;
    private String eventCategoryDescription;
    private Integer eventDuration;
}
