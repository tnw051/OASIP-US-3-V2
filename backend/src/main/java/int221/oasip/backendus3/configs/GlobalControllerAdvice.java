package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.dtos.ApiError;
import int221.oasip.backendus3.exceptions.FieldNotValidException;
import int221.oasip.backendus3.exceptions.ValidationErrors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalControllerAdvice {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public ResponseEntity<Object> handleMaxSizeException() {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(new ApiError(status, "File too large! Maximum file size is " + maxFileSize));
    }

    @ExceptionHandler({BindException.class})
    public ResponseEntity<ApiError> handleBindException(BindException exception) {
        return ApiError.fromException(exception).toResponseEntity();
    }

    @ExceptionHandler({FieldNotValidException.class})
    public ResponseEntity<ApiError> handleFieldNotValidException(FieldNotValidException exception) {
        return ApiError.fromException(exception).toResponseEntity();
    }

    @ExceptionHandler({ResponseStatusException.class})
    public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException exception) {
        return ApiError.fromException(exception).toResponseEntity();
    }

    @ExceptionHandler({ValidationErrors.class})
    public ResponseEntity<ApiError> handleValidationErrors(ValidationErrors exception) {
        return ApiError.fromException(exception).toResponseEntity();
    }
}
