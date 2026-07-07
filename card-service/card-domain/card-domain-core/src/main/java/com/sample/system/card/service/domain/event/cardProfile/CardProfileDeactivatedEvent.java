package com.sample.system.card.service.domain.event.cardProfile;

import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Domain event fired when a card profile is deactivated
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardProfileDeactivatedEvent implements DomainEvent<CardProfile> {

    private final CardProfile cardProfile;
    private final ZonedDateTime deactivatedAt;
    private final String deactivatedBy;

    public CardProfileDeactivatedEvent(CardProfile cardProfile) {
        this.cardProfile = cardProfile;
        this.deactivatedAt = ZonedDateTime.now();
        this.deactivatedBy = "SYSTEM"; // Default value, can be enhanced later
    }

}
