package com.sample.system.card.service.domain.event.card;

import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Domain event fired when a card is deactivated
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardDeactivatedEvent implements DomainEvent<Card> {

    private final Card card;
    private final ZonedDateTime deactivatedAt;
    private final String deactivatedBy;
    private final String reason;
}