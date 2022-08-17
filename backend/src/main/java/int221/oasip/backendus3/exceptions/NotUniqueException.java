package int221.oasip.backendus3.exceptions;

import lombok.Getter;

@Getter
public class NotUniqueException extends RuntimeException {
    public NotUniqueException(String message) {
        super(message);
    }

}
