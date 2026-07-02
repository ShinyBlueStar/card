package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.command.cardType.*;
import com.sample.system.card.service.domain.entity.CardType;
import com.sample.system.card.service.domain.event.cardType.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface CardTypeService extends BaseService {

    CardTypeCreatedEvent createCardType(@Valid CreateCardTypeCommand command) throws CardDomainException;

    CardTypeUpdatedEvent updateCardType(@Valid UpdateCardTypeCommand command) throws CardDomainException;

    CardTypeActivatedEvent activateCardType(@Valid ActivateCardTypeCommand command) throws CardDomainException;

    CardTypeDeactivatedEvent deactivateCardType(@Valid DeactivateCardTypeCommand command) throws CardDomainException;

    CardType getCardType(@Valid GetCardTypeQuery query) throws CardDomainException;

    CardType getCardTypeByCode(@Valid GetCardTypeByCodeQuery query) throws CardDomainException;

    List<CardType> getAllCardTypes(@Valid GetAllCardTypesQuery query) throws CardDomainException;

    Page<CardType> listCardTypes(Map<String, String> params, String caller, String ip) throws CardDomainException;

    void validateCardTypeExists(Long cardTypeId) throws CardDomainException;
}
