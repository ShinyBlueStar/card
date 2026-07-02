package com.sample.system.card.service.domain.event.cardType;

import com.sample.system.card.service.domain.entity.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CardTypeActivatedEvent {
    private final CardType cardType;
    private final ZonedDateTime activatedAt;
}
