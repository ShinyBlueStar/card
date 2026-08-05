package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.entity.CardSecret;

/**
 * Application service for card secret operations (PIN/OTP/CVV2).
 * CardController -> CardSecretCommandHandler -> CardSecretService.
 * Input and output are CardSecret entity; handler maps output to response DTOs.
 */
public interface CardSecretService extends BaseService {

    CardSecret generate(CardSecret cardSecret) throws CardDomainException;

    CardSecret validate(CardSecret cardSecret) throws CardDomainException;
}

