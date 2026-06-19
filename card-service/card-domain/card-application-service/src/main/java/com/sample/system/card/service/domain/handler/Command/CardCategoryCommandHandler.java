package com.sample.system.card.service.domain.handler.Command;

import com.sample.system.card.service.domain.command.cardCategory.*;
import com.sample.system.card.service.domain.event.cardCategory.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardCategoryDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardCategoryService;
import com.sample.system.card.service.domain.response.cardCategory.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardCategoryCommandHandler {

    private final CardCategoryService cardCategoryService;
    private final CardCategoryDataMapper cardCategoryDataMapper;

    @Transactional
    public CreateCardCategoryResponse createCardCategory(CreateCardCategoryCommand command) throws CardDomainException {
        log.info("Starting CardCategory creation handler - Name: {}", command.getName());
        CardCategoryCreatedEvent event = cardCategoryService.createCardCategory(command);
        log.debug("Mapping CardCategory to CreateCardCategoryResponse");
        return cardCategoryDataMapper.cardCategoryToCreateResponse(event.getCardCategory(), "CardCategory created successfully");
    }

    @Transactional
    public UpdateCardCategoryResponse updateCardCategory(UpdateCardCategoryCommand command) throws CardDomainException {
        log.info("Starting CardCategory update handler - CategoryId: {}", command.getCardCategoryId());
        CardCategoryUpdatedEvent event = cardCategoryService.updateCardCategory(command);
        log.debug("Mapping CardCategory to UpdateCardCategoryResponse");
        return cardCategoryDataMapper.cardCategoryToUpdateResponse(event.getCardCategory(), "CardCategory updated successfully");
    }

    @Transactional
    public UpdateCardCategoryResponse activateCardCategory(ActivateCardCategoryCommand command) throws CardDomainException {
        log.info("Starting CardCategory activation handler - CategoryId: {}", command.getCardCategoryId());
        CardCategoryActivatedEvent event = cardCategoryService.activateCardCategory(command);
        log.debug("Mapping CardCategory to UpdateCardCategoryResponse");
        return cardCategoryDataMapper.cardCategoryToUpdateResponse(event.getCardCategory(), "CardCategory activated successfully");
    }

    @Transactional
    public UpdateCardCategoryResponse deactivateCardCategory(DeactivateCardCategoryCommand command) throws CardDomainException {
        log.info("Starting CardCategory deactivation handler - CategoryId: {}", command.getCardCategoryId());
        CardCategoryDeactivatedEvent event = cardCategoryService.deactivateCardCategory(command);
        log.debug("Mapping CardCategory to UpdateCardCategoryResponse");
        return cardCategoryDataMapper.cardCategoryToUpdateResponse(event.getCardCategory(), "CardCategory deactivated successfully");
    }
}
