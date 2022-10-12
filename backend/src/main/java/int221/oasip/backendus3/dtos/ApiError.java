package int221.oasip.backendus3.dtos;

//{
//    "timestamp": "2022-10-12T05:18:40.307+00:00",
//    "status": 400,
//    "error": "Bad Request",
//    "path": "/api/events",
//    "message": "Validation failed",
//    "errors": {
//        "eventStartTime": [
//            "Start time must not be null"
//        ]
//    }
//}

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ApiError {
    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;

//    private Map<String, List<String>> errors;

    public ApiError(HttpStatus status, String message) {
        this.timestamp = LocalDateTime.now().toString();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
    }
}
