package com.sample.system.card.service.domain.event.cardProfile;

import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Domain event fired when a card profile is activated
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardProfileActivatedEvent implements DomainEvent<CardProfile> {

    private final CardProfile cardProfile;
    private final ZonedDateTime activatedAt;
    private final String activatedBy;

    public CardProfileActivatedEvent(CardProfile cardProfile) {
        this.cardProfile = cardProfile;
        this.activatedAt = ZonedDateTime.now();
        this.activatedBy = "SYSTEM"; // Default value, can be enhanced later
    }

}
