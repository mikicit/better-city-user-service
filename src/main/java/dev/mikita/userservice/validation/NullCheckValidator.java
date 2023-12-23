package dev.mikita.userservice.validation;

import dev.mikita.userservice.annotation.NullCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullCheckValidator implements ConstraintValidator<NullCheck, Object> {

    @Override
    public void initialize(NullCheck constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return value == null || !value.toString().trim().isEmpty();
    }
}
