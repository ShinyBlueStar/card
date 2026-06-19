package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.cardCategory.*;
import com.sample.system.card.service.domain.entity.CardCategory;
import com.sample.system.card.service.domain.event.cardCategory.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardCategoryDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardCategoryService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.CardCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
class CardCategoryServiceImpl implements CardCategoryService {

    private final CardCategoryRepository repository;
    private final CardCategoryDataMapper cardCategoryDataMapper;

    @Override
    public CardCategoryCreatedEvent createCardCategory(CreateCardCategoryCommand command) throws CardDomainException {
        log.info("Starting CardCategory creation service - Name: {}", command.getName());
        CardCategory cardCategory = cardCategoryDataMapper.createCommandToCardCategory(command);
        CardCategory savedCardCategory = save(cardCategory, command.getName());
        CardCategoryCreatedEvent event = new CardCategoryCreatedEvent(savedCardCategory, ZonedDateTime.now());
        log.info("CardCategory creation process completed successfully - CategoryId: {}, Name: {}",
                savedCardCategory.getId().getValue(), savedCardCategory.getName());
        return event;
    }

    @Override
    public CardCategoryUpdatedEvent updateCardCategory(UpdateCardCategoryCommand command) throws CardDomainException {
        log.info("Starting CardCategory update service - CategoryId: {}", command.getCardCategoryId());
        CardCategory cardCategory = findById(command.getCardCategoryId());
        cardCategoryDataMapper.updateCardCategoryFromCommand(cardCategory, command);
        CardCategory updated = save(cardCategory, cardCategory.getName());
        CardCategoryUpdatedEvent event = new CardCategoryUpdatedEvent(updated, ZonedDateTime.now());
        log.info("CardCategory update process completed successfully - CategoryId: {}", updated.getId().getValue());
        return event;
    }

    @Override
    public CardCategoryActivatedEvent activateCardCategory(ActivateCardCategoryCommand command) throws CardDomainException {
        log.info("Starting CardCategory activation service - CategoryId: {}", command.getCardCategoryId());
        CardCategory cardCategory = findById(command.getCardCategoryId());
        cardCategory.activate();
        CardCategory updated = save(cardCategory, cardCategory.getName());
        CardCategoryActivatedEvent event = new CardCategoryActivatedEvent(updated, ZonedDateTime.now());
        log.info("CardCategory activation process completed successfully - CategoryId: {}", updated.getId().getValue());
        return event;
    }

    @Override
    public CardCategoryDeactivatedEvent deactivateCardCategory(DeactivateCardCategoryCommand command) throws CardDomainException {
        log.info("Starting CardCategory deactivation service - CategoryId: {}", command.getCardCategoryId());
        CardCategory cardCategory = findById(command.getCardCategoryId());
        cardCategory.deactivate();
        CardCategory updated = save(cardCategory, cardCategory.getName());
        CardCategoryDeactivatedEvent event = new CardCategoryDeactivatedEvent(updated, ZonedDateTime.now());
        log.info("CardCategory deactivation process completed successfully - CategoryId: {}", updated.getId().getValue());
        return event;
    }

    @Override
    public CardCategory getCardCategory(GetCardCategoryQuery query) throws CardDomainException {
        log.info("Starting CardCategory retrieval service - CategoryId: {}", query.getCardCategoryId());
        CardCategory cardCategory = findById(query.getCardCategoryId());
        log.info("CardCategory retrieval completed successfully - CategoryId: {}", query.getCardCategoryId());
        return cardCategory;
    }

    private CardCategory save(CardCategory cardCategory, String nameForLog) throws CardDomainException {
        CardCategory saved = repository.save(cardCategory);
        if (saved == null) {
            log.error("Could not save CardCategory with name: {}", nameForLog);
            throw new CardDomainException(String.format(
                    "Could not save CardCategory with name " + nameForLog),
                    StatusService.ACTIVE_CARD_CATEGORY_NOT_FOUND,
                    HttpStatus.BAD_REQUEST
            );
        }
        return saved;
    }

    private CardCategory findById(Long id) throws CardDomainException {
        log.info("Finding CardCategory by ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.error("CardCategory not found with id: {}", id);
                    return new CardDomainException(String.format(
                            "CardCategory not found with id: " + id),
                            StatusService.ACTIVE_CARD_CATEGORY_NOT_FOUND,
                            HttpStatus.BAD_REQUEST
                    );
                });
    }

    @Override
    public CardCategory getCardCategoryByCode(GetCardCategoryByCodeQuery query) throws CardDomainException {
        log.info("Starting CardCategory retrieval by code service - Code: {}", query.getCode());
        Integer code = Integer.parseInt(query.getCode());
        CardCategory cardCategory = repository.findByCode(code)
                .orElseThrow(() -> {
                    log.error("CardCategory not found with code: {}", query.getCode());
                    return new CardDomainException("CardCategory not found with code: " + query.getCode());
                });
        log.info("CardCategory retrieval by code completed successfully - Code: {}", query.getCode());
        return cardCategory;
    }

    @Override
    public List<CardCategory> getAllCardCategories(GetAllCardCategoriesQuery query) throws CardDomainException {
        log.info("Starting CardCategories retrieval service - ActiveOnly: {}", query.getActiveOnly());
        List<CardCategory> cardCategories = query.getActiveOnly() != null && query.getActiveOnly()
                ? repository.findActiveCardCategories()
                : repository.findAll();
        log.info("CardCategories retrieval completed successfully - Count: {}", cardCategories.size());
        return cardCategories;
    }

    @Override
    public org.springframework.data.domain.Page<CardCategory> listCardCategories(Map<String, String> params, String caller, String ip) throws CardDomainException {
        log.info("Starting CardCategories list service");
        org.springframework.data.domain.Page<CardCategory> allCategories = repository.findAllCategories(params);
        return allCategories;
    }

    @Override
    public void validateCardCategoryExists(Long cardCategoryId) throws CardDomainException {
        log.info("Validating card category existence for ID: {}", cardCategoryId);
        GetCardCategoryQuery query = GetCardCategoryQuery.builder().cardCategoryId(cardCategoryId).build();
        getCardCategory(query);
        log.info("CardCategory validation successful for ID: {}", cardCategoryId);
    }
}
