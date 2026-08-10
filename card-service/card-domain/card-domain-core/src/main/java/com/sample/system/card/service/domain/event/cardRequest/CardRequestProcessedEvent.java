package com.sample.system.card.service.domain.event.cardRequest;

import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Domain event fired when a card request is processed
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardRequestProcessedEvent implements DomainEvent<CardRequest> {

    private final CardRequest cardRequest;
    private final ZonedDateTime processedAt;
    private final String processedBy;

    public CardRequestProcessedEvent(CardRequest cardRequest) {
        this.cardRequest = cardRequest;
        this.processedAt = ZonedDateTime.now();
        this.processedBy = "SYSTEM"; // Default value, can be enhanced later
    }

}
