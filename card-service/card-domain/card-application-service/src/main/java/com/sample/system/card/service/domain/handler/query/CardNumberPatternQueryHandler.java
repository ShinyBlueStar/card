package com.sample.system.card.service.domain.handler.query;

import com.sample.system.card.service.domain.command.numberPattern.GetAllCardNumberPatternsQuery;
import com.sample.system.card.service.domain.command.numberPattern.GetCardNumberPatternQuery;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardNumberPatternDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardNumberPatternService;
import com.sample.system.card.service.domain.response.number.CardNumberPatternListResponse;
import com.sample.system.card.service.domain.response.number.GetCardNumberPatternResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class CardNumberPatternQueryHandler {

    private final CardNumberPatternDataMapper cardNumberPatternDataMapper;
    private final CardNumberPatternService cardNumberPatternService;

    public GetCardNumberPatternResponse getCardNumberPattern(GetCardNumberPatternQuery query) throws CardDomainException {
        log.info("Getting card number pattern with id: {}", query.getPatternId());
        var cardNumberPattern = cardNumberPatternService.getCardNumberPattern(query);
        log.info("CardNumberPattern retrieval completed by QueryHandler - PatternId: {}", query.getPatternId());
        return cardNumberPatternDataMapper.patternToGetResponse(cardNumberPattern);
    }

    public List<GetCardNumberPatternResponse> getAllCardNumberPatterns(GetAllCardNumberPatternsQuery query) throws CardDomainException {
        log.info("Getting all card number patterns with status: {}, cardProfileId: {}, belongTo: {}",
                query.getStatus(), query.getCardProfileId(), query.getBelongTo());
        var cardNumberPatterns = cardNumberPatternService.getAllCardNumberPatterns(query);
        log.info("Found {} card number patterns", cardNumberPatterns.size());
        return cardNumberPatternDataMapper.patternsToGetResponses(cardNumberPatterns);
    }

    public CardNumberPatternListResponse listCardNumberPatterns(Map<String, String> map, String caller, String ip) throws CardDomainException {
        var responses = cardNumberPatternService.listCardNumberPatterns(map, caller, ip);
        log.debug("CardNumberPatterns retrieval completed by QueryHandler - Count: {}",
                responses != null ? responses.getContent().size() : 0);
        return cardNumberPatternDataMapper.patternsToGetListResponse(responses);
    }

    public List<GetCardNumberPatternResponse> getPatternsContainingCardNumber(String cardNumber) throws CardDomainException {
        log.info("Finding patterns containing card number: {}", cardNumber);
        var patterns = cardNumberPatternService.getPatternsContainingCardNumber(cardNumber);
        log.info("Found {} patterns containing card number: {}", patterns.size(), cardNumber);
        return cardNumberPatternDataMapper.patternsToGetResponses(patterns);
    }
}
