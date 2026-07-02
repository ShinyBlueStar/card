package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.cardType.*;
import com.sample.system.card.service.domain.entity.CardType;
import com.sample.system.card.service.domain.event.cardType.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardTypeDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardTypeService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.CardTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
class CardTypeServiceImpl implements CardTypeService {

    private final CardTypeRepository repository;
    private final CardTypeDataMapper cardTypeDataMapper;

    @Override
    public CardTypeCreatedEvent createCardType(CreateCardTypeCommand command) throws CardDomainException {
        log.info("Starting CardType creation service - Name: {}", command.getName());
        CardType cardType = cardTypeDataMapper.createCommandToCardType(command);
        CardType savedCardType = repository.save(cardType);
        if (savedCardType == null) {
            log.error("Could not save CardType with name: {}", command.getName());
            throw new CardDomainException("Could not save CardType with name " + command.getName(),
                    StatusService.ERROR_IN_SAVING_CARD,HttpStatus.OK);
        }
        CardTypeCreatedEvent event = new CardTypeCreatedEvent(savedCardType, ZonedDateTime.now());
        log.info("CardType creation process completed successfully - TypeId: {}, Name: {}",
                savedCardType.getId().getValue(), savedCardType.getName());
        return event;
    }

    @Override
    public CardTypeUpdatedEvent updateCardType(UpdateCardTypeCommand command) throws CardDomainException {
        log.info("Starting CardType update service - TypeId: {}", command.getCardTypeId());
        CardType cardType = repository.findById(command.getCardTypeId())
                .orElseThrow(() -> {
                    log.error("CardType not found with ID: {}", command.getCardTypeId());
                    return new CardDomainException("CardType not found: " + command.getCardTypeId(),
                            StatusService.ACTIVE_CARD_TYPE_NOT_FOUND,HttpStatus.BAD_REQUEST);
                });
        cardTypeDataMapper.updateCardTypeFromCommand(cardType, command);
        CardType updated = repository.save(cardType);
        CardTypeUpdatedEvent event = new CardTypeUpdatedEvent(updated, ZonedDateTime.now());
        log.info("CardType update process completed successfully - TypeId: {}", updated.getId().getValue());
        return event;
    }

    @Override
    public CardTypeActivatedEvent activateCardType(ActivateCardTypeCommand command) throws CardDomainException {
        log.info("Starting CardType activation service - TypeId: {}", command.getCardTypeId());
        CardType cardType = repository.findById(command.getCardTypeId())
                .orElseThrow(() -> {
                    log.error("CardType not found with ID: {}", command.getCardTypeId());
                    return new CardDomainException("CardType not found: " + command.getCardTypeId());
                });
        cardType.activate();
        CardType updated = repository.save(cardType);
        CardTypeActivatedEvent event = new CardTypeActivatedEvent(updated, ZonedDateTime.now());
        log.info("CardType activation process completed successfully - TypeId: {}", updated.getId().getValue());
        return event;
    }

    @Override
    public CardTypeDeactivatedEvent deactivateCardType(DeactivateCardTypeCommand command) throws CardDomainException {
        log.info("Starting CardType deactivation service - TypeId: {}", command.getCardTypeId());
        CardType cardType = repository.findById(command.getCardTypeId())
                .orElseThrow(() -> {
                    log.error("CardType not found with ID: {}", command.getCardTypeId());
                    return new CardDomainException("CardType not found: " + command.getCardTypeId());
                });
        cardType.deactivate();
        CardType updated = repository.save(cardType);
        CardTypeDeactivatedEvent event = new CardTypeDeactivatedEvent(updated, ZonedDateTime.now());
        log.info("CardType deactivation process completed successfully - TypeId: {}", updated.getId().getValue());
        return event;
    }

    @Override
    public CardType getCardType(GetCardTypeQuery query) throws CardDomainException {
        log.info("Starting CardType retrieval service - TypeId: {}", query.getCardTypeId());
        CardType cardType = repository.findById(query.getCardTypeId())
                .orElseThrow(() -> {
                    log.error("CardType not found with id: {}", query.getCardTypeId());
                    return new CardDomainException("CardType not found with id: " + query.getCardTypeId());
                });
        log.info("CardType retrieval completed successfully - TypeId: {}", query.getCardTypeId());
        return cardType;
    }

    @Override
    public CardType getCardTypeByCode(GetCardTypeByCodeQuery query) throws CardDomainException {
        log.info("Starting CardType retrieval by code service - Code: {}", query.getCode());
        Integer code = Integer.parseInt(query.getCode());
        CardType cardType = repository.findByCode(code)
                .orElseThrow(() -> {
                    log.error("CardType not found with code: {}", query.getCode());
                    return new CardDomainException("CardType not found with code: " + query.getCode());
                });
        log.info("CardType retrieval by code completed successfully - Code: {}", query.getCode());
        return cardType;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardType> getAllCardTypes(GetAllCardTypesQuery query) throws CardDomainException {
        log.info("Starting CardTypes retrieval service - ActiveOnly: {}", query.getActiveOnly());
        List<CardType> cardTypes = query.getActiveOnly() != null && query.getActiveOnly()
                ? repository.findActiveCardTypes()
                : repository.findAll();
        log.info("CardTypes retrieval completed successfully - Count: {}", cardTypes.size());
        return cardTypes;
    }

    @Override
    public org.springframework.data.domain.Page<CardType> listCardTypes(Map<String, String> params, String caller, String ip) throws CardDomainException {
        log.info("Starting CardTypes list service");
        org.springframework.data.domain.Page<CardType> allTypes = repository.findAllTypes(params);
        return allTypes;
    }

    @Override
    public void validateCardTypeExists(Long cardTypeId) throws CardDomainException {
        log.info("Validating card type existence for ID: {}", cardTypeId);
        GetCardTypeQuery query = GetCardTypeQuery.builder().cardTypeId(cardTypeId).build();
        getCardType(query);
        log.info("CardType validation successful for ID: {}", cardTypeId);
    }
}
