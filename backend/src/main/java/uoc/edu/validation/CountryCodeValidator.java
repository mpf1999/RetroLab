package uoc.edu.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.Set;

public class CountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {

    private static final Set<String> ISO_COUNTRY_CODES = Set.of(Locale.getISOCountries());

    @Override
    public boolean isValid(String countryCode, ConstraintValidatorContext context) {
        if (countryCode == null || countryCode.isBlank()) {
            return true;
        }

        return ISO_COUNTRY_CODES.contains(countryCode);
    }
}
