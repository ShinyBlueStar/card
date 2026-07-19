package com.sample.system.card.service.domain.event.cardNumberPattern;

import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Domain event fired when a card number pattern is created
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardNumberPatternCreatedEvent implements DomainEvent<CardNumberPattern> {

    private final CardNumberPattern cardNumberPattern;
    private final ZonedDateTime createdAt;
    private final String createdBy;

    public CardNumberPatternCreatedEvent(CardNumberPattern cardNumberPattern) {
        this.cardNumberPattern = cardNumberPattern;
        this.createdAt = ZonedDateTime.now();
        this.createdBy = "SYSTEM"; // Default value, can be enhanced later
    }

}
