package uoc.edu.exception;

import java.time.LocalDateTime;

// Generic error with only one message
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
