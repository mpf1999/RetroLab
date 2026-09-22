package uoc.edu.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI retroLabOpenApi() {
        SecurityScheme jwtSecurityScheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description(
                        "Enter the JWT returned by the login endpoint"
                );

        return new OpenAPI()
                .info(
                        new Info()
                                .title("RetroLab API")
                                .version("1.0.0")
                                .description(
                                        "REST API for managing retro consoles, repair workflows, components and diagnostic measurements."
                                )
                                .contact(
                                        new Contact()
                                                .name("Manuel Pérez Feria")
                                )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        jwtSecurityScheme
                                )
                );
    }
}