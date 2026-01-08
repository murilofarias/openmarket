package com.example.openmarket.controller.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AtLeastOneProfileValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneProfile {
    String message() default "At least one profile (buyer or seller) must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
