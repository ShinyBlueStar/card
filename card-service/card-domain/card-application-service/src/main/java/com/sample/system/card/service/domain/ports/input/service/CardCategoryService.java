package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.command.cardCategory.*;
import com.sample.system.card.service.domain.entity.CardCategory;
import com.sample.system.card.service.domain.event.cardCategory.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface CardCategoryService extends BaseService {

    CardCategoryCreatedEvent createCardCategory(@Valid CreateCardCategoryCommand command) throws CardDomainException;

    CardCategoryUpdatedEvent updateCardCategory(@Valid UpdateCardCategoryCommand command) throws CardDomainException;

    CardCategoryActivatedEvent activateCardCategory(@Valid ActivateCardCategoryCommand command) throws CardDomainException;

    CardCategoryDeactivatedEvent deactivateCardCategory(@Valid DeactivateCardCategoryCommand command) throws CardDomainException;

    CardCategory getCardCategory(@Valid GetCardCategoryQuery query) throws CardDomainException;

    CardCategory getCardCategoryByCode(@Valid GetCardCategoryByCodeQuery query) throws CardDomainException;

    List<CardCategory> getAllCardCategories(@Valid GetAllCardCategoriesQuery query) throws CardDomainException;

    Page<CardCategory> listCardCategories(Map<String, String> params, String caller, String ip) throws CardDomainException;

    void validateCardCategoryExists(Long cardCategoryId) throws CardDomainException;
}
