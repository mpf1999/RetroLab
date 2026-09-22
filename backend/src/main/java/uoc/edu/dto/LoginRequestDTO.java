package uoc.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "LoginRequest",
        description = "Credentials used to authenticate an user"
)
public record LoginRequestDTO(

        @Schema(
                description = "Email address",
                example = "user@mail.test"
        )
        @NotBlank
        @Email
        String email,

        @Schema(
                description = "Password",
                example = "AbCdE12345!"
        )
        @NotBlank
        String password
) {
}