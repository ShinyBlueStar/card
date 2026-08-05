package com.sample.system.card.service.domain.handler.Command;

import com.sample.system.card.service.domain.command.card.GenerateSecretCommand;
import com.sample.system.card.service.domain.command.card.ValidateSecretCommand;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardSecretMapper;
import com.sample.system.card.service.domain.entity.CardSecret;
import com.sample.system.card.service.domain.ports.input.service.CardSecretService;
import com.sample.system.card.service.domain.response.card.GenerateSecretResponse;
import com.sample.system.card.service.domain.response.card.ValidateSecretResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for card secret operations (generate, validate).
 * Maps request commands to CardSecret entity and delegates to CardSecretService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardSecretCommandHandler {

    private final CardSecretService cardSecretService;
    private final CardSecretMapper cardSecretMapper;

    /**
     * Generate PIN/OTP/CVV2 via CardSecretService.
     * Request -> CardSecret (mapper); service returns CardSecret; mapper -> GenerateSecretResponse.
     */
    public GenerateSecretResponse generate(GenerateSecretCommand command) throws CardDomainException {
        log.info("CardSecretCommandHandler.generate: start, type={}", command != null ? command.getType() : null);
        CardSecret cardSecret = cardSecretMapper.toCardSecret(command);
        CardSecret result = cardSecretService.generate(cardSecret);
        GenerateSecretResponse response = cardSecretMapper.toGenerateSecretResponse(result);
        log.info("CardSecretCommandHandler.generate: completed, type={}", result != null ? result.getSecretType() : null);
        return response;
    }

    /**
     * Validate PIN/OTP/CVV2 via CardSecretService.
     * Request -> CardSecret (mapper); service returns CardSecret; mapper -> ValidateSecretResponse.
     */
    public ValidateSecretResponse validate(ValidateSecretCommand command) throws CardDomainException {
        log.info("CardSecretCommandHandler.validate: start, type={}", command != null ? command.getType() : null);
        CardSecret cardSecret = cardSecretMapper.toCardSecret(command);
        CardSecret result = cardSecretService.validate(cardSecret);
        ValidateSecretResponse response = cardSecretMapper.toValidateSecretResponse(result);
        log.info("CardSecretCommandHandler.validate: completed, valid={}", result != null ? result.getValid() : null);
        return response;
    }
}
