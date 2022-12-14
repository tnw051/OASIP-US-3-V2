package int221.oasip.backendus3.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryOwnerResponse {
    private Integer id;
    private String ownerEmail;
    private Integer eventCategoryId;
}
