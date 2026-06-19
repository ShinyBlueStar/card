package com.sample.system.card.service.domain.event.cardCategory;

import com.sample.system.card.service.domain.entity.CardCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CardCategoryActivatedEvent {
    private final CardCategory cardCategory;
    private final ZonedDateTime activatedAt;
}
