package com.sample.system.card.service.domain.command.card;

import com.sample.system.card.service.domain.anotation.ValidValidateSecretCommand;
import com.sample.system.card.service.domain.enums.SecretType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Command for validating PIN/OTP/CVV2
 * PAN is received in request, cardId is resolved from PAN
 */
@Getter
@Setter
@ValidValidateSecretCommand
public class ValidateSecretCommand {

    @NotBlank(message = "PAN is required")
    @Size(min = 16, max = 16, message = "PAN must be 16 digits (YYMM)")
    @Pattern(regexp = "\\d+", message = "PAN must be numeric")
    private String pan;

    @NotNull(message = "type must not be null")
    private SecretType type;

    private String value;

    @Size(min = 4, max = 4, message = "expTime must be 4 digits (YYMM)")
    @Pattern(regexp = "\\d+", message = "expTime must be numeric")
    private String expTime;

    @Size(min = 3, max = 3, message = "serviceCode must be 3 digits")
    @Pattern(regexp = "\\d+", message = "serviceCode must be numeric")
    private String serviceCode;
}
