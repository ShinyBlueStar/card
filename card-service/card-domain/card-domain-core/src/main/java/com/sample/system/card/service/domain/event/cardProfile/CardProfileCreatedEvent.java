package com.sample.system.card.service.domain.event.cardProfile;

import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Domain event fired when a card profile is created
 * Following DDD event-driven architecture principles
 */
@Getter
@AllArgsConstructor
public class CardProfileCreatedEvent implements DomainEvent<CardProfile> {

    private final CardProfile cardProfile;
    private final ZonedDateTime createdAt;
    private final String createdBy;

    public CardProfileCreatedEvent(CardProfile cardProfile) {
        this.cardProfile = cardProfile;
        this.createdAt = ZonedDateTime.now();
        this.createdBy = "SYSTEM"; // Default value, can be enhanced later
    }

}