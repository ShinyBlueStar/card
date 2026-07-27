package com.sample.system.card.service.domain.event.card;

import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public class CardCreatedEvent implements DomainEvent<Card> {

    private final Card card;

    private final ZonedDateTime createdAt;

    public CardCreatedEvent(Card card, ZonedDateTime createdAt) {
        this.card = card;
        this.createdAt = createdAt;
    }

    public Card getCard() {
        return card;
    }
}