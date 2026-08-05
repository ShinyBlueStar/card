package com.sample.system.card.service.domain.anotation;

import com.sample.system.card.service.domain.command.card.ValidateSecretCommand;
import com.sample.system.card.service.domain.enums.SecretType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidateSecretCommandValidator
        implements ConstraintValidator<ValidValidateSecretCommand, ValidateSecretCommand> {

    @Override
    public boolean isValid(ValidateSecretCommand command,
                           ConstraintValidatorContext context) {

        if (command == null) {
            return true;
        }

        if ( SecretType.OTP == command.getType() || SecretType.PIN == command.getType()) {

            boolean valid = true;

            if (command.getValue() == null || command.getValue().isBlank()) {
                addViolation(context, "value is required when type is PIN or OTP", "value");
                valid = false;
            }

            return valid;
        }

        if (SecretType.CVV == command.getType()) {

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
