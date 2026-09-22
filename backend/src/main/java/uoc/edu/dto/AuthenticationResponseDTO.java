package uoc.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(
        name= "AuthenticationResponse",
        description = "Authentication token returned as successful login"
)
public record AuthenticationResponseDTO(

        @Schema(
                description = "JWT used to access protected endpoints",
                example= "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRruR9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ"
        )
        String token,
        @Schema(
                description = "Token validity period in ms",
                example = "3600000"
        )
        long expiresIn
) {
}
