package com.sample.system.card.service.domain.ports.output.externalService;

import com.sample.system.card.service.domain.entity.CardSecret;
import com.sample.system.card.service.domain.entity.Session;
import com.sample.system.card.service.domain.exception.CardDomainException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface SSMService {

    String getPin1(@NotNull UUID cardId, @NotBlank String nationalCode, @NotBlank String cardNumber) throws CardDomainException;

    String getPin2(@NotNull UUID cardId, @NotBlank String nationalCode, @NotBlank String cardNumber) throws CardDomainException;

    String getCvv2(UUID cardId, String cardNumber,
                   int cvv2Length, String expTime, String serviceCode) throws CardDomainException;

    String callGenerate(CardSecret cardSecret) throws Exception;

    String callValidate(CardSecret cardSecret) throws Exception;

    Session getOrCreateSession(UUID cardId, String channel, String sessionType, String clientFingerprint) throws CardDomainException;

    UUID validateAndGetCardId(UUID sessionId) throws CardDomainException;

    void invalidateSession(UUID sessionId);
}

