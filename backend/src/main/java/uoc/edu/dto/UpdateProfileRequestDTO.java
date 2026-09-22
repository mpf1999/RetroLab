package uoc.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(
        name = "UpdateProfileRequest",
        description = """
                Name and email fields that can be modified from the user profile.
                At least one field must be provided.
                """
)
public record UpdateProfileRequestDTO(

        @Schema(
                description = "New user name",
                example = "fulanillo",
                nullable = true
        )
        @Size(
                max = 100,
                message = "Name cannot exceed 100 characters"
        )
        @Pattern(
                regexp = ".*\\S.*",
                message = "Name cannot be blank"
        )
        String name,

        @Schema(
                description = "New user email address",
                example = "smbdy@mail.test",
                nullable = true
        )
        @Email(message = "Email must be valid")
        @Size(
                max = 255,
                message = "Email cannot exceed 255 characters"
        )
        String email

) {

    @AssertTrue(
            message = "At least one profile field must be provided"
    )
    @Schema(hidden = true)
    public boolean isAnyFieldProvided() {
        return name != null || email != null;
    }
}