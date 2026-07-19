package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.valueObject.CardNumberDto;

/**
 * Bank Application Service Interface
 * Defines operations for Bank management
 * Following Hexagonal Architecture - Input Port
 */
public interface CardNumberGeneratorService extends BaseService {
    String process(CardNumberDto cardNumberDto) throws CardDomainException;
}
