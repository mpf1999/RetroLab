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
import uoc.edu.dto.ManufacturerRequestDTO;
import uoc.edu.dto.ManufacturerResponseDTO;
import uoc.edu.service.ManufacturerService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manufacturers")
@Tag(
        name = "Manufacturers",
        description = "Operations for managing console manufacturers"
)
@SecurityRequirement(
        name = SwaggerConfig.SECURITY_SCHEME_NAME
)
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    public ManufacturerController(
            ManufacturerService manufacturerService
    ) {
        this.manufacturerService = manufacturerService;
    }

    @GetMapping
    @Operation(
            summary = "Get all manufacturers",
            description = """
                    Returns every console manufacturer registered in RetroLab.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Manufacturers retrieved successfully"
    )
    public List<ManufacturerResponseDTO> getManufacturers() {
        return manufacturerService.getAllManufacturers();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a manufacturer",
            description = """
                    Returns a manufacturer using its identifier.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Manufacturer found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Manufacturer not found",
                    content = @Content
            )
    })
    public ManufacturerResponseDTO getManufacturerById(
            @Parameter(
                    description = "Manufacturer identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        return manufacturerService.getManufacturerById(id);
    }

    @PostMapping
    @Operation(
            summary = "Create a manufacturer",
            description = """
                    Registers a new console manufacturer in RetroLab.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Manufacturer created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid manufacturer data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A manufacturer with this name already exists",
                    content = @Content
            )
    })
    public ResponseEntity<ManufacturerResponseDTO> addManufacturer(
            @Valid
            @RequestBody
            ManufacturerRequestDTO manufacturerRequestDTO
    ) {
        ManufacturerResponseDTO createdManufacturer =
                manufacturerService.addManufacturer(
                        manufacturerRequestDTO
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdManufacturer);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a manufacturer",
            description = """
                    Updates the name and country code of an existing
                    manufacturer.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Manufacturer updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid manufacturer data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Manufacturer not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A manufacturer with this name already exists",
                    content = @Content
            )
    })
    public ResponseEntity<ManufacturerResponseDTO> updateManufacturer(
            @Parameter(
                    description = "Manufacturer identifier",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            ManufacturerRequestDTO manufacturerRequestDTO
    ) {
        ManufacturerResponseDTO updatedManufacturer =
                manufacturerService.updateManufacturer(
                        id,
                        manufacturerRequestDTO
                );

        return ResponseEntity.ok(updatedManufacturer);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a manufacturer",
            description = """
                    Deletes a manufacturer together with its console models
                    and all dependent data through cascade.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = """
                            Manufacturer and dependent data deleted successfully
                            """
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Manufacturer not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteManufacturer(
            @Parameter(
                    description = "Manufacturer identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        manufacturerService.deleteManufacturer(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}