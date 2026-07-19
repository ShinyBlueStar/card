package com.sample.system.card.service.domain.event.cardNumberPattern;

import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Domain event fired when a card number pattern is updated
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardNumberPatternUpdatedEvent implements DomainEvent<CardNumberPattern> {

    private final CardNumberPattern cardNumberPattern;
    private final ZonedDateTime updatedAt;
    private final String updatedBy;

    public CardNumberPatternUpdatedEvent(CardNumberPattern cardNumberPattern) {
        this.cardNumberPattern = cardNumberPattern;
        this.updatedAt = ZonedDateTime.now();
        this.updatedBy = "SYSTEM"; // Default value, can be enhanced later
    }

}
