package com.sample.system.card.service.domain.event.cardRequest;

import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Domain event fired when a card request is cancelled
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardRequestCancelledEvent implements DomainEvent<CardRequest> {

    private final CardRequest cardRequest;
    private final ZonedDateTime cancelledAt;
    private final String cancelledBy;

    public CardRequestCancelledEvent(CardRequest cardRequest) {
        this.cardRequest = cardRequest;
        this.cancelledAt = ZonedDateTime.now();
        this.cancelledBy = "SYSTEM"; // Default value, can be enhanced later
    }
}
