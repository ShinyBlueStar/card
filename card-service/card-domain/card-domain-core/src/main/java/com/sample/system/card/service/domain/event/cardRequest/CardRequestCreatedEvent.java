package com.sample.system.card.service.domain.event.cardRequest;

import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Domain event fired when a card request is created
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardRequestCreatedEvent implements DomainEvent<CardRequest> {

    private final CardRequest cardRequest;
    private final ZonedDateTime createdAt;
    private final String createdBy;

    public CardRequestCreatedEvent(CardRequest cardRequest) {
        this.cardRequest = cardRequest;
        this.createdAt = ZonedDateTime.now();
        this.createdBy = "SYSTEM"; // Default value, can be enhanced later
    }

}
