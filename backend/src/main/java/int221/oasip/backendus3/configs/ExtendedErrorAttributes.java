package int221.oasip.backendus3.configs;

import int221.oasip.backendus3.exceptions.FieldNotValidException;
import int221.oasip.backendus3.exceptions.ValidationErrors;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        Throwable error = super.getError(webRequest);
        String VALIDATION_ERROR_MESSAGE = "Validation failed";
        String MESSAGE_ATTRIBUTE = "message";
        String ERRORS_ATTRIBUTE = "errors";

        if (error instanceof BindException) {
            errorAttributes.put(MESSAGE_ATTRIBUTE, VALIDATION_ERROR_MESSAGE);
            errorAttributes.put(ERRORS_ATTRIBUTE, makeValidationErrorMap((BindException) error));
        }

        if (error instanceof FieldNotValidException) {
            errorAttributes.put(MESSAGE_ATTRIBUTE, VALIDATION_ERROR_MESSAGE);
            errorAttributes.put(ERRORS_ATTRIBUTE, makeValidationErrorMap((FieldNotValidException) error));
        }

        if (error instanceof ResponseStatusException) {
            ResponseStatusException exception = (ResponseStatusException) error;
            errorAttributes.put(MESSAGE_ATTRIBUTE, exception.getReason());
        }

        if (error instanceof ValidationErrors) {
            errorAttributes.put(MESSAGE_ATTRIBUTE, VALIDATION_ERROR_MESSAGE);
            errorAttributes.put(ERRORS_ATTRIBUTE, ((ValidationErrors) error).getErrors());
        }

        return errorAttributes;
    }

    private Map<String, List<String>> makeValidationErrorMap(BindException exception) {
        Map<String, List<String>> errorMaps = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errorMaps.computeIfAbsent(fieldError.getField(), k -> new ArrayList<>()).add(fieldError.getDefaultMessage());
        }
        return errorMaps;
    }

    private Map<String, List<String>> makeValidationErrorMap(FieldNotValidException exception) {
        Map<String, List<String>> errorMaps = new HashMap<>();
        errorMaps.put(exception.getField(), List.of(exception.getMessage()));
        return errorMaps;
    }
}
