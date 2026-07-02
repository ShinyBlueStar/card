package com.sample.system.card.service.domain.handler.Command;

import com.sample.system.card.service.domain.command.cardType.*;
import com.sample.system.card.service.domain.event.cardType.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardTypeDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardTypeService;
import com.sample.system.card.service.domain.response.cardType.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardTypeCommandHandler {

    private final CardTypeService cardTypeService;
    private final CardTypeDataMapper cardTypeDataMapper;

    @Transactional
    public CreateCardTypeResponse createCardType(CreateCardTypeCommand command) throws CardDomainException {
        log.info("Starting CardType creation handler - Name: {}", command.getName());
        CardTypeCreatedEvent event = cardTypeService.createCardType(command);
        log.debug("Mapping CardType to CreateCardTypeResponse");
        return cardTypeDataMapper.cardTypeToCreateResponse(event.getCardType(), "CardType created successfully");
    }

    @Transactional
    public UpdateCardTypeResponse updateCardType(UpdateCardTypeCommand command) throws CardDomainException {
        log.info("Starting CardType update handler - TypeId: {}", command.getCardTypeId());
        CardTypeUpdatedEvent event = cardTypeService.updateCardType(command);
        log.debug("Mapping CardType to UpdateCardTypeResponse");
        return cardTypeDataMapper.cardTypeToUpdateResponse(event.getCardType(), "CardType updated successfully");
    }

    @Transactional
    public UpdateCardTypeResponse activateCardType(ActivateCardTypeCommand command) throws CardDomainException {
        log.info("Starting CardType activation handler - TypeId: {}", command.getCardTypeId());
        CardTypeActivatedEvent event = cardTypeService.activateCardType(command);
        log.debug("Mapping CardType to UpdateCardTypeResponse");
        return cardTypeDataMapper.cardTypeToUpdateResponse(event.getCardType(), "CardType activated successfully");
    }

    @Transactional
    public UpdateCardTypeResponse deactivateCardType(DeactivateCardTypeCommand command) throws CardDomainException {
        log.info("Starting CardType deactivation handler - TypeId: {}", command.getCardTypeId());
        CardTypeDeactivatedEvent event = cardTypeService.deactivateCardType(command);
        log.debug("Mapping CardType to UpdateCardTypeResponse");
        return cardTypeDataMapper.cardTypeToUpdateResponse(event.getCardType(), "CardType deactivated successfully");
    }
}
