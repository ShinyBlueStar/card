package com.sample.system.card.service.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
public class CreateCardResponse {
    @NotNull
    private final Long cardId;
    @NotNull
    private final String message;
}
