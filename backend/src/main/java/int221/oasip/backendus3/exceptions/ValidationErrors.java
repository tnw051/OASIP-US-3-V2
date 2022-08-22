package int221.oasip.backendus3.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationErrors extends RuntimeException {
    private final Map<String, Collection<String>> errors = new HashMap<>();

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

    public Map<String, Collection<String>> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

}
