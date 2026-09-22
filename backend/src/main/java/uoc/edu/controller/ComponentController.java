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
import org.springframework.web.bind.annotation.*;
import uoc.edu.config.SwaggerConfig;
import uoc.edu.dto.ComponentRequestDTO;
import uoc.edu.dto.ComponentResponseDTO;
import uoc.edu.service.ComponentService;

import java.util.List;

@RestController
@RequestMapping("api/v1/components")
@Tag(
        name= "Components",
        description = "Operations for managing console model components"
)
@SecurityRequirement(
        name = SwaggerConfig.SECURITY_SCHEME_NAME
)
public class ComponentController {

    private final ComponentService componentService;

    public ComponentController(ComponentService componentService) {
        this.componentService = componentService;
    }

    @GetMapping
    @Operation(
            summary = "Get all components",
            description = "Returns every component registered in RetroLab."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Components retrieved successfully"
    )
    public List<ComponentResponseDTO> getComponents() {
        return componentService.getAllComponents();
    }

    @GetMapping("{id}")
    @Operation(
            summary = "Get a component",
            description = "Returns a component using its id."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Component found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Component not found",
                    content = @Content
            )
    })
    public ComponentResponseDTO getComponentById(
            @Parameter(
                    description = "Component id",
                    example = "1"
            )
            @PathVariable Long id) {
        return componentService.getComponentById(id);
    }

    @PostMapping
    @Operation(
            summary = "Create a component",
            description = """
                    Creates a component and associates it with an existing
                    console model.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Component created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid component data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Console model not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A component with the same name already exists for the selected console model",
                    content = @Content
            )
    })
    public ResponseEntity<ComponentResponseDTO> addComponent(@Valid @RequestBody ComponentRequestDTO component) {
        return ResponseEntity.status(HttpStatus.CREATED).body(componentService.addComponent(component));
    }

    @PutMapping("{id}")
    @Operation(
            summary = "Update a component",
            description = """
                    Updates the component identified by the supplied ID.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Component updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid component data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Component or console model not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                            A component with the same name already exists
                            for the selected console model
                            """,
                    content = @Content
            )
    })
    public ResponseEntity<ComponentResponseDTO> updateComponent(
            @Parameter(
                    description = "Component id",
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody ComponentRequestDTO componentRequestDTO) {
        return ResponseEntity.ok(componentService.updateComponent(id, componentRequestDTO));
    }

    @DeleteMapping("{id}")
    @Operation(
            summary = "Delete a component",
            description = """
                    Deletes a component and its associated diagnostic tests.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Component deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Component not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteComponent(
            @Parameter(
                    description = "Component id",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        componentService.deleteComponent(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
