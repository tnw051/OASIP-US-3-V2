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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import int221.oasip.backendus3.exceptions.FieldNotValidException;
import int221.oasip.backendus3.exceptions.ValidationErrors;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    static String VALIDATION_ERROR_MESSAGE = "Validation failed";

    private final String timestamp;
    @JsonIgnore
    private final HttpStatus httpStatus;
    private final int status;
    private final String error;
    private final String message;

    private Map<String, List<String>> errors;

    public ApiError(HttpStatus httpStatus, String message) {
        this.timestamp = Instant.now().toString();
        this.httpStatus = httpStatus;
        this.status = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.message = message;
    }

    public ResponseEntity<ApiError> toResponseEntity() {
        return new ResponseEntity<>(this, httpStatus);
    }

    public static ApiError fromException(FieldNotValidException e) {
        ApiError apiError = makeApiErrorForBadRequest(VALIDATION_ERROR_MESSAGE);
        apiError.errors = makeValidationErrorMap(e);
        return apiError;
    }

    public static ApiError fromException(BindException e) {
        ApiError apiError = makeApiErrorForBadRequest(VALIDATION_ERROR_MESSAGE);
        apiError.errors = makeValidationErrorMap(e);
        return apiError;
    }

    public static ApiError fromException(ResponseStatusException e) {
        return new ApiError(e.getStatus(), e.getReason());
    }

    public static ApiError fromException(ValidationErrors e) {
        ApiError apiError = makeApiErrorForBadRequest(VALIDATION_ERROR_MESSAGE);
        apiError.errors = e.getErrors();
        return apiError;
    }

    public static ApiError fromException(Exception e) {
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    private static ApiError makeApiErrorForBadRequest(String validationErrorMessage) {
        return new ApiError(HttpStatus.BAD_REQUEST, validationErrorMessage);
    }

    private static Map<String, List<String>> makeValidationErrorMap(BindException exception) {
        Map<String, List<String>> errorMaps = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errorMaps.computeIfAbsent(fieldError.getField(), k -> new ArrayList<>()).add(fieldError.getDefaultMessage());
        }
        return errorMaps;
    }

    private static Map<String, List<String>> makeValidationErrorMap(FieldNotValidException exception) {
        Map<String, List<String>> errorMaps = new HashMap<>();
        errorMaps.put(exception.getField(), List.of(exception.getMessage()));
        return errorMaps;
    }
}
