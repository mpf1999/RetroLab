package uoc.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uoc.edu.config.SwaggerConfig;
import uoc.edu.dto.ConsoleModelRequestDTO;
import uoc.edu.dto.ConsoleModelResponseDTO;
import uoc.edu.service.ConsoleModelService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/console-models")
@Tag(
        name = "Console Models",
        description = "Operations for managing console models"
)
@SecurityRequirement(
        name = SwaggerConfig.SECURITY_SCHEME_NAME
)
public class ConsoleModelController {

    private final ConsoleModelService consoleModelService;

    public ConsoleModelController(
            ConsoleModelService consoleModelService
    ) {
        this.consoleModelService = consoleModelService;
    }

    @GetMapping
    @Operation(
            summary = "Get all console models",
            description = """
                    Returns every console model registered in RetroLab.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Console models retrieved successfully"
    )
    public List<ConsoleModelResponseDTO> getConsoleModels() {
        return consoleModelService.getAllConsoleModels();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a console model",
            description = """
                    Returns a console model using its identifier.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Console model found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Console model not found",
                    content = @Content
            )
    })
    public ConsoleModelResponseDTO getConsoleModelById(
            @Parameter(
                    description = "Console model identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        return consoleModelService.getConsoleModelById(id);
    }

    @PostMapping
    @Operation(
            summary = "Create a console model",
            description = """
                    Creates a console model and associates it with an
                    existing manufacturer.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Console model created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid console model data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Manufacturer not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A console model with this name already exists",
                    content = @Content
            )
    })
    public ResponseEntity<ConsoleModelResponseDTO> addConsoleModel(
            @Valid
            @RequestBody
            ConsoleModelRequestDTO consoleModelRequestDTO
    ) {
        ConsoleModelResponseDTO createdConsoleModel =
                consoleModelService.addConsoleModel(
                        consoleModelRequestDTO
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdConsoleModel);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a console model",
            description = """
                    Updates the name, release year and manufacturer of an
                    existing console model.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Console model updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid console model data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Console model or manufacturer not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A console model with this name already exists",
                    content = @Content
            )
    })
    public ResponseEntity<ConsoleModelResponseDTO> updateConsoleModel(
            @Parameter(
                    description = "Console model identifier",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            ConsoleModelRequestDTO consoleModelRequestDTO
    ) {
        ConsoleModelResponseDTO updatedConsoleModel =
                consoleModelService.updateConsoleModel(
                        id,
                        consoleModelRequestDTO
                );

        return ResponseEntity.ok(updatedConsoleModel);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a console model",
            description = """
                    Deletes a console model together with its associated
                    components, consoles, repair cases and component tests
                    through cascade.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = """
                            Console model and dependent data deleted successfully
                            """
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Console model not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteConsoleModel(
            @Parameter(
                    description = "Console model identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        consoleModelService.deleteConsoleModel(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}