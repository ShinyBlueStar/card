package com.sample.system.card.service.domain.anotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenerateSecretCommandValidator.class)
@Documented
public @interface ValidGenerateSecretCommand {

    String message() default "Invalid secret generation parameters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}