package int221.oasip.backendus3.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileInfoResponse {
    private String name;
    private String type;
    private String bucketId;
}
