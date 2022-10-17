package int221.oasip.backendus3.dtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class EditEventMultipartRequest extends EditEventRequest {
    private MultipartFile file;
}
