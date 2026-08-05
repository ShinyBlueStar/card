package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.card.GenerateSecretCommand;
import com.sample.system.card.service.domain.command.card.ValidateSecretCommand;
import com.sample.system.card.service.domain.entity.CardSecret;
import com.sample.system.card.service.domain.enums.SecretType;
import com.sample.system.card.service.domain.response.card.GenerateSecretResponse;
import com.sample.system.card.service.domain.response.card.ValidateSecretResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper: commands -> CardSecret; CardSecret -> response DTOs.
 */
@Slf4j
@Component
public class CardSecretMapper {

    /**
     * Convert GenerateSecretCommand to CardSecret (cardId and nationalId set by service).
     */
    public CardSecret toCardSecret(GenerateSecretCommand command) {
        log.info("CardSecretMapper.toCardSecret(GenerateSecretCommand): mapping command to CardSecret");
        if (command == null) {
            return null;
        }
        return CardSecret.builder()
                .pan(command.getPan())
                .secretType(command.getType())
                .expTime(command.getExpTime())
                .serviceCode(command.getServiceCode())
                .channel(command.getChannel())
                .sessionType(command.getSessionType())
                .clientFingerprint(command.getClientFingerprint())
                .encryptedData(command.getType() == SecretType.CVV? command.getPan()+command.getExpTime()+command.getServiceCode():null)
                .reqType("generate")
                .build();
    }

    /**
     * Convert ValidateSecretCommand to CardSecret (cardId and nationalId set by service).
     */
    public CardSecret toCardSecret(ValidateSecretCommand command) {
        log.info("CardSecretMapper.toCardSecret(ValidateSecretCommand): mapping command to CardSecret");
        if (command == null) {
            return null;
        }
        return CardSecret.builder()
                .pan(command.getPan())
                .secretType(command.getType())
                .value(command.getType() == SecretType.CVV? command.getPan()+command.getExpTime()+command.getServiceCode():command.getValue())
                .reqType("validate")
                .build();
    }

    /**
     * Convert CardSecret (generate result) to GenerateSecretResponse.
     */
    public GenerateSecretResponse toGenerateSecretResponse(CardSecret cardSecret) {
        log.info("CardSecretMapper.toGenerateSecretResponse: mapping CardSecret to GenerateSecretResponse");
        if (cardSecret == null) {
            return null;
        }
        return GenerateSecretResponse.builder()
                .type(cardSecret.getSecretType() != null ? cardSecret.getSecretType().name() : null)
                .value(cardSecret.getValue())
                .build();
    }

    /**
     * Convert CardSecret (validate result) to ValidateSecretResponse.
     */
    public ValidateSecretResponse toValidateSecretResponse(CardSecret cardSecret) {
        log.info("CardSecretMapper.toValidateSecretResponse: mapping CardSecret to ValidateSecretResponse");
        if (cardSecret == null) {
            return null;
        }
        return ValidateSecretResponse.builder()
                .valid(Boolean.TRUE.equals(cardSecret.getValid()))
                .message(cardSecret.getMessage())
                .build();
    }
}
