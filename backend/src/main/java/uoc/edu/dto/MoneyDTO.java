package uoc.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Schema(
        name = "Money",
        description = "Monetary amount and its three-letter currency code"
)
public record MoneyDTO(

        @Schema(
                description = """
                        Monetary amount expressed in the specified currency.
                        A maximum of two decimal places is allowed.
                        """,
                example = "120.00",
                minimum = "0.0"
        )
        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.0",
                message = "Amount cannot be negative"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = """
                        Amount must have up to 10 integer digits and
                        2 decimal places
                        """
        )
        BigDecimal amount,

        @Schema(
                description = "Three-letter ISO 4217 currency code",
                example = "EUR",
                pattern = "^[A-Za-z]{3}$",
                minLength = 3,
                maxLength = 3
        )
        @NotBlank(message = "Currency is required")
        @Pattern(
                regexp = "^[A-Za-z]{3}$",
                message = "Currency must be a three-letter ISO code"
        )
        String currency

) {
}