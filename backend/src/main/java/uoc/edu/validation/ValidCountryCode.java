package uoc.edu.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CountryCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCountryCode {

    String message() default "Country code must be a valid ISO alpha-2 code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
