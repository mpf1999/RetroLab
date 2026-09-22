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
import uoc.edu.dto.ComponentTestRequestDTO;
import uoc.edu.dto.ComponentTestResponseDTO;
import uoc.edu.service.ComponentTestService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/component-tests")
@Tag(
        name = "Component Tests",
        description = "Operations for managing component tests"
)
@SecurityRequirement(
        name = SwaggerConfig.SECURITY_SCHEME_NAME
)
public class ComponentTestController {

    private final ComponentTestService componentTestService;

    public ComponentTestController(
            ComponentTestService componentTestService
    ) {
        this.componentTestService = componentTestService;
    }

    @GetMapping
    @Operation(
            summary = "Get all component tests",
            description = """
                    Returns every component diagnostic test registered
                    in RetroLab.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Component tests retrieved successfully"
    )
    public List<ComponentTestResponseDTO> getComponentTests() {
        return componentTestService.getAllComponentTests();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a component test",
            description = "Returns a component diagnostic test using its identifier."

    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Component test found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Component test not found",
                    content = @Content
            )
    })
    public ComponentTestResponseDTO getComponentTestById(
            @Parameter(
                    description = "Component test identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        return componentTestService.getComponentTestById(id);
    }

    @GetMapping("/repair-case/{repairCaseId}")
    @Operation(
            summary = "Get tests by repair case",
            description = """
                    Returns all component tests associated with a particular
                    repair case.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Component tests retrieved successfully"
    )
    public List<ComponentTestResponseDTO>
    getComponentTestsByRepairCaseId(
            @Parameter(
                    description = "Repair case identifier",
                    example = "1"
            )
            @PathVariable Long repairCaseId
    ) {
        return componentTestService
                .getComponentTestsByRepairCaseId(
                        repairCaseId
                );
    }

    @GetMapping("/component/{componentId}")
    @Operation(
            summary = "Get tests by component",
            description = """
                    Returns all diagnostic tests performed on a particular
                    component.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Component tests retrieved successfully"
    )
    public List<ComponentTestResponseDTO>
    getComponentTestsByComponentId(
            @Parameter(
                    description = "Component identifier",
                    example = "1"
            )
            @PathVariable Long componentId
    ) {
        return componentTestService
                .getComponentTestsByComponentId(
                        componentId
                );
    }

    @PostMapping
    @Operation(
            summary = "Create a component test",
            description = """
                    Registers diagnostic measurements for a component
                    within an existing repair case.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Component test created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid measurement or request data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Repair case or component not found",
                    content = @Content
            )
    })
    public ResponseEntity<ComponentTestResponseDTO>
    addComponentTest(
            @Valid
            @RequestBody
            ComponentTestRequestDTO componentTestRequestDTO
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        componentTestService.addComponentTest(
                                componentTestRequestDTO
                        )
                );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a component test",
            description = """
                    Updates the measurements and result of an existing
                    component diagnostic test.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Component test updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid measurement or request data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            Component test, repair case or component not found
                            """,
                    content = @Content
            )
    })
    public ResponseEntity<ComponentTestResponseDTO>
    updateComponentTest(
            @Parameter(
                    description = "Component test identifier",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            ComponentTestRequestDTO componentTestRequestDTO
    ) {
        return ResponseEntity.ok(
                componentTestService.updateComponentTest(
                        id,
                        componentTestRequestDTO
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a component test",
            description = """
                    Permanently deletes a component diagnostic test.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Component test deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Component test not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteComponentTest(
            @Parameter(
                    description = "Component test identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        componentTestService.deleteComponentTest(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}