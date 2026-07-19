package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.command.numberPattern.GetAllCardNumberPatternsQuery;
import com.sample.system.card.service.domain.command.numberPattern.GetCardNumberPatternQuery;
import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.enums.CardNumberPatternStatus;
import com.sample.system.card.service.domain.event.cardNumberPattern.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface CardNumberPatternService extends BaseService {
    CardNumberPatternCreatedEvent createCardNumberPattern(CardNumberPattern cardNumberPattern)
            throws CardDomainException;
    CardNumberPatternUpdatedEvent updateCardNumberPattern(CardNumberPattern cardNumberPattern)
            throws CardDomainException;
    CardNumberPatternUpdatedEvent deactivateCardNumberPattern(CardNumberPattern cardNumberPattern)
            throws CardDomainException;
    CardNumberPattern getCardNumberPattern(GetCardNumberPatternQuery query)
            throws CardDomainException;
    List<CardNumberPattern> getAllCardNumberPatterns(GetAllCardNumberPatternsQuery query)
            throws CardDomainException;
    Page<CardNumberPattern> listCardNumberPatterns(Map<String, String> map, String caller, String ip)
            throws CardDomainException;
    List<CardNumberPattern> getPatternsContainingCardNumber(String cardNumber)
            throws CardDomainException;
    CardNumberPatternsFetchedEvent createCardNumberPatterns(
            List<CardNumberPattern> cardNumberPatterns) throws CardDomainException;

    boolean existByStatus(Long numPatternId, CardNumberPatternStatus cardNumberPatternStatus);
    CardNumberPattern save(CardNumberPattern cardNumberPattern) throws CardDomainException;
    List<CardNumberPattern> findByCardProfileId(Long cardProfileId) throws CardDomainException;
    List<CardNumberPattern> findAll() throws CardDomainException;
    Page<CardNumberPattern> findAll(Map<String, String> searchCriteria) throws CardDomainException;
    CardNumberPattern getActivePatternForProfile(CardProfile profile) throws CardDomainException;
    String getNextAndIncrement(CardNumberPattern patt) throws CardDomainException;
}