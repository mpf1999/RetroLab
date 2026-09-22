package uoc.edu.exception;

import java.time.LocalDateTime;
import java.util.List;
// When different @Valid errors are found, for that reason List<String>
public record ApiValidationError(
        LocalDateTime timestamp,
        int status,
        String error,
        List<String> errors
) {
}
