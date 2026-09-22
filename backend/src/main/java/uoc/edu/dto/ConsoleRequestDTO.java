package uoc.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uoc.edu.model.Condition;
import uoc.edu.model.Status;

@Schema(
        name = "ConsoleRequest",
        description = """
                Information required to create or update a console.
                A console can belong to a registered user through ownerId,
                or to an external owner through ownerName.
                """
)
public record ConsoleRequestDTO(

        @Schema(
                description = """
                        Identifier of the registered owner. Leave this field
                        empty when ownerName is provided.
                        """,
                example = "2",
                nullable = true
        )
        Long ownerId,

        @Schema(
                description = """
                        Name of an owner who is not registered in RetroLab.
                        Leave this field empty when ownerId is provided.
                        """,
                example = "External Customer",
                maxLength = 100,
                nullable = true
        )
        @Size(
                max = 100,
                message = "Owner name cannot exceed 100 characters"
        )
        String ownerName,

        @Schema(
                description = "Identifier of the console model",
                example = "1"
        )
        @NotNull(message = "Console model ID is required")
        Long consoleModelId,

        @Schema(
                description = "Unique serial number of the console",
                example = "GB-TEST-001",
                maxLength = 45
        )
        @NotBlank(message = "Serial number is required")
        @Size(
                max = 45,
                message = "Maximum length is 45 characters"
        )
        String serialNumber,

        @Schema(
                description = "Regional version of the console",
                example = "PAL"
        )
        @NotBlank(message = "Region is required")
        String region,

        @Schema(
                description = "Console colour",
                example = "Grey",
                maxLength = 30
        )
        @NotBlank(message = "Color is required")
        @Size(
                max = 30,
                message = "Maximum length is 30 characters"
        )
        String color,

        @Schema(
                description = "Estimated monetary value of the console"
        )
        @NotNull(message = "Estimated value is required")
        @Valid
        MoneyDTO estimatedValue,

        @Schema(
                description = "Physical condition of the console",
                example = "GOOD",
                allowableValues = {
                        "EXCELLENT",
                        "GOOD",
                        "FAIR",
                        "POOR",
                        "BROKEN"
                }
        )
        @NotNull(message = "Condition is required")
        Condition condition,

        @Schema(
                description = "Current status of the console",
                example = "IN_REPAIR"
        )
        @NotNull(message = "Status is required")
        Status status,

        @Schema(
                description = "Additional observations about the console",
                example = "The console does not power on",
                maxLength = 2000,
                nullable = true
        )
        @Size(
                max = 2000,
                message = "Maximum length is 2000 characters"
        )
        String notes

) {
}