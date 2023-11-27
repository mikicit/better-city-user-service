package dev.mikita.userservice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Firebase authorization.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FirebaseAuthorization {
    /**
     * Roles string [ ].
     *
     * @return the string [ ]
     */
    String[] roles() default {};

    /**
     * Statuses string [ ].
     *
     * @return the string [ ]
     */
    String[] statuses() default {};
}