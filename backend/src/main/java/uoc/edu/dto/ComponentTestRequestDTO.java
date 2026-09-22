package uoc.edu.dto;

import jakarta.validation.constraints.*;
import uoc.edu.model.TestResult;

import java.math.BigDecimal;

public record ComponentTestRequestDTO(
        @NotNull(message = "Repair case ID is required")
        Long repairCaseId,

        @NotNull(message = "Component ID is required")
        Long componentId,

        @DecimalMin(value = "0.0", message = "Voltage cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Voltage must have up to 5 integer digits and 2 decimal places")
        BigDecimal measuredVoltage,

        @DecimalMin(value = "0.0", message = "Current cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Current must have up to 5 integer digits and 2 decimal places")
        BigDecimal measuredCurrent,

        @DecimalMin(value = "0.0", message = "Resistance cannot be negative")
        @Digits(integer = 5, fraction = 2, message = "Resistance must have up to 5 integer digits and 2 decimal places")
        BigDecimal measuredResistance,

        @Digits(integer = 3, fraction = 2, message = "Temperature must have up to 3 integer digits and 2 decimal places")
        BigDecimal temperature,

        Boolean continuity,

        @NotNull(message = "Test result is required")
        TestResult result,

        @Size(max = 2000, message = "Maximum length is 2000 characters")
        String notes
) {
}
