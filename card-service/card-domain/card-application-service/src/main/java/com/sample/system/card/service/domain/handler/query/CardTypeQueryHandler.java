package com.sample.system.card.service.domain.handler.query;

import com.sample.system.card.service.domain.command.cardType.*;
import com.sample.system.card.service.domain.entity.CardType;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardTypeDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardTypeService;
import com.sample.system.card.service.domain.response.cardType.CardTypeListResponse;
import com.sample.system.card.service.domain.response.cardType.GetCardTypeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardTypeQueryHandler {

    private final CardTypeService cardTypeService;
    private final CardTypeDataMapper cardTypeDataMapper;

    @Transactional(readOnly = true)
    public GetCardTypeResponse getCardType(GetCardTypeQuery query) throws CardDomainException {
        log.info("Getting card type with id: {}", query.getCardTypeId());
        CardType cardType = cardTypeService.getCardType(query);
        log.info("CardType retrieval completed by QueryHandler - TypeId: {}", query.getCardTypeId());
        return cardTypeDataMapper.cardTypeToGetResponse(cardType);
    }

    @Transactional(readOnly = true)
    public GetCardTypeResponse getCardTypeByCode(GetCardTypeByCodeQuery query) throws CardDomainException {
        log.info("Getting card type with code: {}", query.getCode());
        CardType cardType = cardTypeService.getCardTypeByCode(query);
        log.info("CardType retrieval by code completed by QueryHandler - Code: {}", query.getCode());
        return cardTypeDataMapper.cardTypeToGetResponse(cardType);
    }

    public List<GetCardTypeResponse> getAllCardTypes(GetAllCardTypesQuery query) throws CardDomainException {
        log.info("Getting all card types with activeOnly: {}", query.getActiveOnly());
        List<CardType> cardTypes = cardTypeService.getAllCardTypes(query);
        log.info("Found {} card types", cardTypes.size());
        return cardTypeDataMapper.cardTypesToGetResponses(cardTypes);
    }

    public CardTypeListResponse listCardTypes(Map<String, String> map, String caller, String ip) throws CardDomainException {
        org.springframework.data.domain.Page<CardType> responses = cardTypeService.listCardTypes(map, caller, ip);
        log.debug("CardTypes retrieval completed by QueryHandler - Count: {}",
                responses != null ? responses.getContent().size() : 0);
        return cardTypeDataMapper.cardTypesToGetListResponse(responses);
    }
}
