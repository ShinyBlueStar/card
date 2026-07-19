package com.sample.system.card.service.domain.response.number;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateCardNumberPatternResponse  {
    @NotNull
    private final Long cardNumberPatternId;
    @NotNull
    private final String message;
}

