package com.sample.system.card.service.domain.event.card;

import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CardUnblockedEvent implements DomainEvent<Card> {

    private final Card card;
}

