package com.sample.system.card.service.domain.response.cardType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateCardTypeResponse {
    private final Long cardTypeId;
    private final String message;
    private final Boolean isActive;
}