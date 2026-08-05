package com.sample.system.card.service.domain.anotation;

import com.sample.system.card.service.domain.command.card.GenerateSecretCommand;
import com.sample.system.card.service.domain.enums.SecretType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GenerateSecretCommandValidator
        implements ConstraintValidator<ValidGenerateSecretCommand, GenerateSecretCommand> {

    @Override
    public boolean isValid(GenerateSecretCommand command,
                           ConstraintValidatorContext context) {

        if (command == null) {
            return true;
        }

        if (command.getType() == SecretType.CVV) {

            boolean valid = true;

            if (command.getExpTime() == null || command.getExpTime().isBlank()) {
                addViolation(context, "expTime is required when type is CVV", "expTime");
                valid = false;
            }

            if (command.getServiceCode() == null || command.getServiceCode().isBlank()) {
                addViolation(context, "serviceCode is required when type is CVV", "serviceCode");
                valid = false;
            }

            return valid;
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext context,
                              String message,
                              String fieldName) {

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(fieldName)
                .addConstraintViolation();
    }
}
