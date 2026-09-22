package uoc.edu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uoc.edu.model.Role;

public record UserRequestDTO(
        @NotBlank(message = "A name is required")
        @Size(min = 2, max = 25, message = "Name cannot exceed 25 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 40, message = "Password must be between 8 and 40 characters")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d).*$",
                message = "Password must contain at least one uppercase letter and one number"
        )
        String password,
        Role role) {
}
