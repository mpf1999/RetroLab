package uoc.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uoc.edu.validation.ValidCountryCode;

public record ManufacturerRequestDTO(
        @NotBlank(message = "Manufacturer name is required")
        @Size(min = 2, max = 80, message = "Manufacturer name must be between 2 and 80 characters")
        String manufacturerName,

        @NotBlank(message = "Country code is required")
        @Pattern(
                regexp = "^[A-Z]{2}$",
                message = "Country code must be a valid ISO alpha-2 code, for example JP, ES or US"
        )
        @ValidCountryCode
        String countryCode
) {
}
