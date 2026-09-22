package uoc.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uoc.edu.model.RepairStatus;

public record RepairCaseRequestDTO(
        @NotNull(message = "Console ID is required")
        Long consoleId,

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        @NotNull(message = "Repair status is required")
        RepairStatus status
) {
}
