package com.sample.system.card.service.domain.event.cardRequest;

import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Domain event fired when a card request is updated
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardRequestUpdatedEvent implements DomainEvent<CardRequest> {

    private final CardRequest cardRequest;
    private final ZonedDateTime updatedAt;
    private final String updatedBy;

    public CardRequestUpdatedEvent(CardRequest cardRequest) {
        this.cardRequest = cardRequest;
        this.updatedAt = ZonedDateTime.now();
        this.updatedBy = "SYSTEM"; // Default value, can be enhanced later
    }
}
