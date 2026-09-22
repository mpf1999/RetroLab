package uoc.edu.dto;

import jakarta.validation.constraints.NotNull;
import uoc.edu.model.Role;

public record ChangeRoleRequestDTO(
        @NotNull(message = "Role is required")
        Role role
) {
}
