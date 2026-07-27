package com.sample.system.card.service.domain;

import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.event.card.CardCreatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CardDomainServiceImpl implements CardDomainService {

    @Override
    public CardCreatedEvent validateAndInitiateCardProfile(CardProfile profile) throws CardDomainException {
        return null;
    }

    @Override
    public CardCreatedEvent validateAndInitiateCardRequest(CardRequest request) {
       return null;    }

    @Override
    public CardCreatedEvent validateAndInitiateCardNumberPattern(CardNumberPattern pattern) {
        return null;
    }
}