package com.sample.system.card.service.domain.handler.query;

import com.sample.system.card.service.domain.command.cardCategory.*;
import com.sample.system.card.service.domain.entity.CardCategory;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardCategoryDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardCategoryService;
import com.sample.system.card.service.domain.response.cardCategory.CardCategoryListResponse;
import com.sample.system.card.service.domain.response.cardCategory.GetCardCategoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardCategoryQueryHandler {

    private final CardCategoryService cardCategoryService;
    private final CardCategoryDataMapper cardCategoryDataMapper;

    @Transactional(readOnly = true)
    public GetCardCategoryResponse getCardCategory(GetCardCategoryQuery query) throws CardDomainException {
        log.info("Getting card category with id: {}", query.getCardCategoryId());
        CardCategory cardCategory = cardCategoryService.getCardCategory(query);
        log.info("CardCategory retrieval completed by QueryHandler - CategoryId: {}", query.getCardCategoryId());
        return cardCategoryDataMapper.cardCategoryToGetResponse(cardCategory);
    }

    @Transactional(readOnly = true)
    public GetCardCategoryResponse getCardCategoryByCode(GetCardCategoryByCodeQuery query) throws CardDomainException {
        log.info("Getting card category with code: {}", query.getCode());
        CardCategory cardCategory = cardCategoryService.getCardCategoryByCode(query);
        log.info("CardCategory retrieval by code completed by QueryHandler - Code: {}", query.getCode());
        return cardCategoryDataMapper.cardCategoryToGetResponse(cardCategory);
    }

    public List<GetCardCategoryResponse> getAllCardCategories(GetAllCardCategoriesQuery query) throws CardDomainException {
        log.info("Getting all card categories with activeOnly: {}", query.getActiveOnly());
        List<CardCategory> cardCategories = cardCategoryService.getAllCardCategories(query);
        log.info("Found {} card categories", cardCategories.size());
        return cardCategoryDataMapper.cardCategoriesToGetResponses(cardCategories);
    }

    public CardCategoryListResponse listCardCategories(Map<String, String> map, String caller, String ip) throws CardDomainException {
        org.springframework.data.domain.Page<CardCategory> responses = cardCategoryService.listCardCategories(map, caller, ip);
        log.debug("CardCategories retrieval completed by QueryHandler - Count: {}",
                responses != null ? responses.getContent().size() : 0);
        return cardCategoryDataMapper.cardCategoriesToGetListResponse(responses);
    }
}
