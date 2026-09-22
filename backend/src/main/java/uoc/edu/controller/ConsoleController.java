package uoc.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uoc.edu.config.SwaggerConfig;
import uoc.edu.dto.ConsoleRequestDTO;
import uoc.edu.dto.ConsoleResponseDTO;
import uoc.edu.service.ConsoleService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consoles")
@Tag(
        name = "Consoles",
        description = "Operations for managing retro consoles and their images"
)
@SecurityRequirement(
        name = SwaggerConfig.SECURITY_SCHEME_NAME
)
public class ConsoleController {

    private final ConsoleService consoleService;

    public ConsoleController(
            ConsoleService consoleService
    ) {
        this.consoleService = consoleService;
    }

    @GetMapping
    @Operation(
            summary = "Get all consoles",
            description = "Returns every console registered in RetroLab."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Consoles retrieved successfully"
    )
    public List<ConsoleResponseDTO> getConsoles() {
        return consoleService.getAllConsoles();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a console",
            description = "Returns a console using its identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Console found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Console not found",
                    content = @Content
            )
    })
    public ConsoleResponseDTO getConsoleById(
            @Parameter(
                    description = "Console identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        return consoleService.getConsoleById(id);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Create a console",
            description = """
                    Registers a console and associates it with a console model.
                    The owner can be a registered user or an external owner.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Console created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid console or owner data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Console model or registered owner not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The serial number is already registered",
                    content = @Content
            )
    })
    public ResponseEntity<ConsoleResponseDTO> addConsole(
            @Valid
            @RequestBody
            ConsoleRequestDTO consoleRequestDTO
    ) {
        ConsoleResponseDTO createdConsole =
                consoleService.addConsole(
                        consoleRequestDTO
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdConsole);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Update a console",
            description = """
                    Updates the information of an existing console.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Console updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid console or owner data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            Console, console model or registered owner not found
                            """,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The serial number is already registered",
                    content = @Content
            )
    })
    public ResponseEntity<ConsoleResponseDTO> updateConsole(
            @Parameter(
                    description = "Console identifier",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            ConsoleRequestDTO consoleRequestDTO
    ) {
        ConsoleResponseDTO updatedConsole =
                consoleService.updateConsole(
                        id,
                        consoleRequestDTO
                );

        return ResponseEntity.ok(updatedConsole);
    }

    @PostMapping(
            value = "/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Upload a console image",
            description = """
                    Uploads or replaces the image associated with a console.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Console image uploaded successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The supplied image is invalid",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Console not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "The uploaded image exceeds the size limit",
                    content = @Content
            )
    })
    public ResponseEntity<ConsoleResponseDTO>
    uploadConsoleImage(
            @Parameter(
                    description = "Console identifier",
                    example = "1"
            )
            @PathVariable Long id,

            @Parameter(
                    description = "Image file associated with the console",
                    required = true,
                    schema = @Schema(
                            type = "string",
                            format = "binary"
                    )
            )
            @RequestPart("image")
            MultipartFile image
    ) {
        ConsoleResponseDTO updatedConsole =
                consoleService.updateConsoleImage(
                        id,
                        image
                );

        return ResponseEntity.ok(updatedConsole);
    }

    @DeleteMapping("/{id}/image")
    @Operation(
            summary = "Delete a console image",
            description = """
                    Removes the image associated with a console.
                    The console itself is not deleted.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Console image deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Console not found",
                    content = @Content
            )
    })
    public ResponseEntity<ConsoleResponseDTO>
    deleteConsoleImage(
            @Parameter(
                    description = "Console identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        ConsoleResponseDTO updatedConsole =
                consoleService.deleteConsoleImage(id);

        return ResponseEntity.ok(updatedConsole);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a console",
            description = "Deletes a console and its dependent data (repair cases)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Console deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Console not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteConsole(
            @Parameter(
                    description = "Console identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        consoleService.deleteConsole(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}