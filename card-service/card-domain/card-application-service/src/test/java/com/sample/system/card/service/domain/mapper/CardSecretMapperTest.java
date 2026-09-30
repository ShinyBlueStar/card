package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.card.GenerateSecretCommand;
import com.sample.system.card.service.domain.command.card.ValidateSecretCommand;
import com.sample.system.card.service.domain.entity.CardSecret;
import com.sample.system.card.service.domain.enums.SecretType;
import com.sample.system.card.service.domain.response.card.GenerateSecretResponse;
import com.sample.system.card.service.domain.response.card.ValidateSecretResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardSecretMapperTest {

    private final CardSecretMapper mapper = new CardSecretMapper();

    private static GenerateSecretCommand generate(SecretType type) {
        GenerateSecretCommand command = new GenerateSecretCommand();
        command.setPan("6037991200000011");
        command.setType(type);
        command.setExpTime("2912");
        command.setServiceCode("120");
        command.setChannel("API");
        command.setSessionType("CARD_ISSUANCE");
        command.setClientFingerprint("fp");
        return command;
    }

    private static ValidateSecretCommand validate(SecretType type) {
        ValidateSecretCommand command = new ValidateSecretCommand();
        command.setPan("6037991200000011");
        command.setType(type);
        command.setValue("1234");
        command.setExpTime("2912");
        command.setServiceCode("120");
        return command;
    }

    @Test
    void generateCommandIsMapped() {
        CardSecret secret = mapper.toCardSecret(generate(SecretType.PIN));

        assertThat(secret.getPan()).isEqualTo("6037991200000011");
        assertThat(secret.getSecretType()).isEqualTo(SecretType.PIN);
        assertThat(secret.getChannel()).isEqualTo("API");
        assertThat(secret.getSessionType()).isEqualTo("CARD_ISSUANCE");
        assertThat(secret.getClientFingerprint()).isEqualTo("fp");
        assertThat(secret.getReqType()).isEqualTo("generate");
        assertThat(secret.getEncryptedData()).isNull();
    }

    @Test
    void generateCommandForCvvCarriesThePanExpiryAndServiceCode() {
        CardSecret secret = mapper.toCardSecret(generate(SecretType.CVV));

        assertThat(secret.getEncryptedData()).isEqualTo("6037991200000011" + "2912" + "120");
    }

    @Test
    void validateCommandIsMapped() {
        CardSecret secret = mapper.toCardSecret(validate(SecretType.OTP));

        assertThat(secret.getValue()).isEqualTo("1234");
        assertThat(secret.getReqType()).isEqualTo("validate");
    }

    @Test
    void validateCommandForCvvIgnoresTheSuppliedValue() {
        assertThat(mapper.toCardSecret(validate(SecretType.CVV)).getValue()).isEqualTo("6037991200000011" + "2912" + "120");
    }

    @Test
    void nullCommandsAndSecretsMapToNull() {
        assertThat(mapper.toCardSecret((GenerateSecretCommand) null)).isNull();
        assertThat(mapper.toCardSecret((ValidateSecretCommand) null)).isNull();
        assertThat(mapper.toGenerateSecretResponse(null)).isNull();
        assertThat(mapper.toValidateSecretResponse(null)).isNull();
    }

    @Test
    void generateResponseCarriesTypeAndValue() {
        GenerateSecretResponse response = mapper.toGenerateSecretResponse(
                CardSecret.builder().secretType(SecretType.PIN).value("4321").build());
        GenerateSecretResponse untyped = mapper.toGenerateSecretResponse(CardSecret.builder().value("x").build());

        assertThat(response.getType()).isEqualTo("PIN");
        assertThat(response.getValue()).isEqualTo("4321");
        assertThat(untyped.getType()).isNull();
    }

    @Test
    void validateResponseTreatsMissingVerdictAsInvalid() {
        ValidateSecretResponse valid = mapper.toValidateSecretResponse(CardSecret.builder().valid(true).message("ok").build());
        ValidateSecretResponse unknown = mapper.toValidateSecretResponse(CardSecret.builder().build());

        assertThat(valid.isValid()).isTrue();
        assertThat(valid.getMessage()).isEqualTo("ok");
        assertThat(unknown.isValid()).isFalse();
    }
}
