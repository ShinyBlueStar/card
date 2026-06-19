package com.sample.system.card.service.domain.command.cardCategory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DeactivateCardCategoryCommand {
    @NotNull(message = "CardCategory ID is required")
    private final Long cardCategoryId;
}