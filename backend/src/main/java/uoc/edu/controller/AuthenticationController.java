package uoc.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uoc.edu.dto.AuthenticationResponseDTO;
import uoc.edu.dto.LoginRequestDTO;
import uoc.edu.exception.ApiError;
import uoc.edu.security.JwtService;
import uoc.edu.service.AuthenticationService;

// Entry point for login, "login controller"
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    // check email and password
    private final AuthenticationService authenticationService;
    // create and manage JWT
    private final JwtService jwtService;

    public AuthenticationController(
            AuthenticationService authenticationService,
            JwtService jwtService
    ) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    //check that there is a valid login, then create authentication token and validate
    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = """
                    Validates the email and password. If the credentials are valid, returns a JWT that can be used to access the endpoints."""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication was successful",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthenticationResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The request contains invalid data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "The email or password is not correct",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
        }
    )
    public ResponseEntity<AuthenticationResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {

        String token = authenticationService.login(
                request.email(),
                request.password()
        );

        // after authentication, generate the JWT token and return the pertinent AuthenticationResponseDTO
        AuthenticationResponseDTO response =
                new AuthenticationResponseDTO(
                        token,
                        jwtService.getExpirationTime()
                );

        return ResponseEntity.ok(response);
    }
}