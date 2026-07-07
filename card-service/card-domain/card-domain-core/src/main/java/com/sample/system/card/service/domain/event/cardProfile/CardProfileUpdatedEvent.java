package com.sample.system.card.service.domain.event.cardProfile;

import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Domain event fired when a card profile is updated
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardProfileUpdatedEvent implements DomainEvent<CardProfile> {

    private final CardProfile cardProfile;
    private final ZonedDateTime updatedAt;
    private final String updatedBy;

    public CardProfileUpdatedEvent(CardProfile cardProfile) {
        this.cardProfile = cardProfile;
        this.updatedAt = ZonedDateTime.now();
        this.updatedBy = "SYSTEM"; // Default value, can be enhanced later
    }

}
