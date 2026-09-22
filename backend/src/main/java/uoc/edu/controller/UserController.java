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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uoc.edu.config.SwaggerConfig;
import uoc.edu.dto.ChangeRoleRequestDTO;
import uoc.edu.dto.UpdateProfileRequestDTO;
import uoc.edu.dto.UserRequestDTO;
import uoc.edu.dto.UserResponseDTO;
import uoc.edu.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Tag(
        name = "Users",
        description = "Operations for managing RetroLab users and profiles"
)
@SecurityRequirement(
        name = SwaggerConfig.SECURITY_SCHEME_NAME
)
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Returns every user registered in RetroLab."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully"
    )
    public List<UserResponseDTO> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get a user",
            description = "Returns a user using its identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public UserResponseDTO getUserById(
            @Parameter(
                    description = "User identifier",
                    example = "2"
            )
            @PathVariable Long userId
    ) {
        return userService.getUserById(userId);
    }

    @PostMapping
    @Operation(
            summary = "Create a user",
            description = "Registers a new user in RetroLab."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A user with this email already exists",
                    content = @Content
            )
    })
    public ResponseEntity<UserResponseDTO> addUser(
            @Valid
            @RequestBody
            UserRequestDTO request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        userService.addUser(request)
                );
    }

    @PutMapping("/{userId}")
    @Operation(
            summary = "Update a user",
            description = """
                    Updates the complete information of an existing user.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The email is already in use",
                    content = @Content
            )
    })
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(
                    description = "User identifier",
                    example = "2"
            )
            @PathVariable Long userId,

            @Valid
            @RequestBody
            UserRequestDTO request
    ) {
        return ResponseEntity.ok(
                userService.updateUser(
                        userId,
                        request
                )
        );
    }

    @PatchMapping("/{userId}/profile")
    @Operation(
            summary = "Update profile details",
            description = """
                Updates the name, email address or both fields of an
                existing user. Password and role are not modified.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid profile data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The email is already in use",
                    content = @Content
            )
    })
    public ResponseEntity<UserResponseDTO>
    updateProfileDetails(
            @Parameter(
                    description = "User identifier",
                    example = "2"
            )
            @PathVariable Long userId,

            @Valid
            @RequestBody
            UpdateProfileRequestDTO request
    ) {
        return ResponseEntity.ok(
                userService.updateProfileDetails(
                        userId,
                        request.name(),
                        request.email()
                )
        );
    }


    @PatchMapping("/{userId}/role")
    @Operation(
            summary = "Change a user's role",
            description = """
                    Changes the role assigned to a user. This operation is
                    restricted to administrators.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User role changed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid role",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Administrator permissions are required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<UserResponseDTO> changeUserRole(
            @Parameter(
                    description = "User id",
                    example = "2"
            )
            @PathVariable Long userId,

            @Valid
            @RequestBody
            ChangeRoleRequestDTO request
    ) {
        return ResponseEntity.ok(
                userService.changeRole(
                        userId,
                        request
                )
        );
    }

    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Delete a user",
            description = "Deletes a user and its dependent data through cascade"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User and dependent data deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(
                    description = "User id",
                    example = "2"
            )
            @PathVariable Long userId
    ) {
        userService.deleteUser(userId);

        return ResponseEntity
                .noContent()
                .build();
    }
}