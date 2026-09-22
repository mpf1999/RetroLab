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
import uoc.edu.dto.RepairCaseRequestDTO;
import uoc.edu.dto.RepairCaseResponseDTO;
import uoc.edu.service.RepairCaseService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/repair-cases")
@Tag(
        name = "Repair Cases",
        description = "Operations for managing console repair"
)
@SecurityRequirement(
        name = SwaggerConfig.SECURITY_SCHEME_NAME
)
public class RepairCaseController {

    private final RepairCaseService repairCaseService;

    public RepairCaseController(
            RepairCaseService repairCaseService
    ) {
        this.repairCaseService = repairCaseService;
    }

    @GetMapping
    @Operation(
            summary = "Get all repair cases",
            description = """
                    Returns every repair case registered.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Repair cases retrieved successfully"
    )
    public List<RepairCaseResponseDTO> getRepairCases() {
        return repairCaseService.getAllRepairCases();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a repair case",
            description = """
                    Returns a repair case using its identifier.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Repair case found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Repair case not found",
                    content = @Content
            )
    })
    public RepairCaseResponseDTO getRepairCaseById(
            @Parameter(
                    description = "Repair case identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        return repairCaseService.getRepairCaseById(id);
    }

    @GetMapping("/consoles/{consoleId}")
    @Operation(
            summary = "Get repair cases by console",
            description = "Returns all repair cases associated with a console."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Repair cases retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Console not found",
                    content = @Content
            )
    })
    public List<RepairCaseResponseDTO>
    getRepairCasesByConsoleId(
            @Parameter(
                    description = "Console identifier",
                    example = "1"
            )
            @PathVariable Long consoleId
    ) {
        return repairCaseService.getRepairCasesByConsoleId(
                consoleId
        );
    }

    @PostMapping
    @Operation(
            summary = "Create a repair case",
            description = """
                    Creates a repair case and associates it with an
                    existing console.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Repair case created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid repair case data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Console not found",
                    content = @Content
            )
    })
    public ResponseEntity<RepairCaseResponseDTO> addRepairCase(
            @Valid
            @RequestBody
            RepairCaseRequestDTO repairCaseRequestDTO
    ) {
        RepairCaseResponseDTO createdRepairCase =
                repairCaseService.addRepairCase(
                        repairCaseRequestDTO
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdRepairCase);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a repair case",
            description = """
                    Updates the title, description, status and associated
                    console of an existing repair case.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Repair case updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid repair case data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Repair case or console not found",
                    content = @Content
            )
    })
    public ResponseEntity<RepairCaseResponseDTO>
    updateRepairCase(
            @Parameter(
                    description = "Repair case identifier",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            RepairCaseRequestDTO repairCaseRequestDTO
    ) {
        RepairCaseResponseDTO updatedRepairCase =
                repairCaseService.updateRepairCase(
                        id,
                        repairCaseRequestDTO
                );

        return ResponseEntity.ok(updatedRepairCase);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a repair case",
            description = """
                    Deletes a repair case together with its associated
                    component tests through cascade.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = """
                            Repair case and dependent component tests
                            deleted successfully
                            """
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Repair case not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteRepairCase(
            @Parameter(
                    description = "Repair case identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        repairCaseService.deleteRepairCase(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}