package com.sample.system.card.service.domain.response.cardCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateCardCategoryResponse {
    private final Long cardCategoryId;
    private final String message;
    private final Boolean isActive;
}