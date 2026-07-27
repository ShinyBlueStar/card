package com.sample.system.card.service.domain;

import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.event.card.CardCreatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;

public interface CardDomainService {

    CardCreatedEvent validateAndInitiateCardProfile(CardProfile profile) throws CardDomainException;

    CardCreatedEvent validateAndInitiateCardRequest(CardRequest request);

    CardCreatedEvent validateAndInitiateCardNumberPattern(CardNumberPattern pattern);

}
