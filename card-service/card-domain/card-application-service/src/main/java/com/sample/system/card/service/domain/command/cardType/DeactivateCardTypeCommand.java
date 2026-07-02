package com.sample.system.card.service.domain.command.cardType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DeactivateCardTypeCommand {
    @NotNull(message = "CardType ID is required")
    private final Long cardTypeId;
}