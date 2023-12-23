package dev.mikita.userservice.annotation;

import dev.mikita.userservice.validation.NullCheckValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@Target({FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NullCheckValidator.class)
public @interface NullCheck {

    // Error message
    String message() default "Value cannot be null or empty.";

    // Represents the group of constraints
    Class<?>[] groups() default {};


    // Represents additional information about annotation
    Class<? extends Payload>[] payload() default {};
}

