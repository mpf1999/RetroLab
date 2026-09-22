package uoc.edu.dto;

import jakarta.validation.constraints.*;

public record ComponentRequestDTO(
        @NotNull(message = "Console model ID is required")
        Long consoleModelId,

        @NotBlank(message = "Component name is required")
        @Size(min = 2, max = 80, message = "Component name must be between 2 and 80 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description
) {
}
