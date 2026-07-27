package com.sample.system.card.service.domain.response.card;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateCardResponse  {
    @NotNull
    private final Long cardId;
    @NotNull
    private final String message;
}
