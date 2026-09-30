package com.sample.system.card.service.domain.handler;

import com.sample.system.card.service.domain.command.card.GenerateSecretCommand;
import com.sample.system.card.service.domain.command.card.ValidateSecretCommand;
import com.sample.system.card.service.domain.entity.CardSecret;
import com.sample.system.card.service.domain.enums.SecretType;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardSecretCommandHandler;
import com.sample.system.card.service.domain.mapper.CardSecretMapper;
import com.sample.system.card.service.domain.ports.input.service.CardSecretService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardSecretHandlerTest {

    @Mock
    private CardSecretService service;

    @Test
    void generateReturnsTheIssuedSecret() throws CardDomainException {
        CardSecretCommandHandler handler = new CardSecretCommandHandler(service, new CardSecretMapper());
        when(service.generate(any(CardSecret.class))).thenReturn(CardSecret.builder().secretType(SecretType.PIN).value("4321").build());
        GenerateSecretCommand command = new GenerateSecretCommand();
        command.setPan("6037991200000011");
        command.setType(SecretType.PIN);

        var response = handler.generate(command);

        assertThat(response.getType()).isEqualTo("PIN");
        assertThat(response.getValue()).isEqualTo("4321");
    }

    @Test
    void validateReturnsTheVerdict() throws CardDomainException {
        CardSecretCommandHandler handler = new CardSecretCommandHandler(service, new CardSecretMapper());
        when(service.validate(any(CardSecret.class))).thenReturn(CardSecret.builder().valid(true).message("ok").build());
        ValidateSecretCommand command = new ValidateSecretCommand();
        command.setPan("6037991200000011");
        command.setType(SecretType.PIN);
        command.setValue("1234");

        var response = handler.validate(command);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getMessage()).isEqualTo("ok");
    }

    @Test
    void missingCommandsAndResultsAreTolerated() throws CardDomainException {
        CardSecretCommandHandler handler = new CardSecretCommandHandler(service, new CardSecretMapper());
        when(service.generate(null)).thenReturn(null);
        when(service.validate(null)).thenReturn(null);

        assertThat(handler.generate(null)).isNull();
        assertThat(handler.validate(null)).isNull();
    }
}
