package com.sample.system.card.service.domain.event.cardNumberPattern;

import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Domain event fired when multiple card number patterns are fetched
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardNumberPatternsFetchedEvent implements DomainEvent<List<CardNumberPattern>> {

    private final List<CardNumberPattern> cardNumberPatterns;
    private final ZonedDateTime fetchedAt;
    private final String fetchedBy;

    public CardNumberPatternsFetchedEvent(List<CardNumberPattern> cardNumberPatterns) {
        this.cardNumberPatterns = cardNumberPatterns;
        this.fetchedAt = ZonedDateTime.now();
        this.fetchedBy = "SYSTEM"; // Default value, can be enhanced later
    }

}
