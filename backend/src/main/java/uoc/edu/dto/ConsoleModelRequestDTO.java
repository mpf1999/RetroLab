package uoc.edu.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record ConsoleModelRequestDTO(
        @NotBlank(message = "Console model name is required")
        @Size(min = 2, max = 80, message = "Console model name must be between 2 and 80 characters")
        String consoleModelName,

        @NotNull(message = "Release year is required")
        Integer releaseYear,

        @NotNull(message = "Manufacturer ID is required")
        Long manufacturerId
) {
}
