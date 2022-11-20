package int221.oasip.backendus3.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.*;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationErrors extends RuntimeException {
    private final Map<String, List<String>> errors = new HashMap<>();

    public ValidationErrors() {
        super("Validation errors");
    }

    public void addFieldError(String field, String message) {
        errors.computeIfAbsent(field, k -> new LinkedList<>()).add(message);
    }

    public void addFieldErrors(BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors()) {
            return;
        }
        bindingResult.getFieldErrors().forEach(error -> addFieldError(error.getField(), error.getDefaultMessage()));
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

}
