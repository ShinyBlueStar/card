package com.sample.system.card.service.domain.command.card;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command to activate a card
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class ActivateCardCommand {
    @NotNull
    private final Long cardId;
    @NotNull
    private Long ReasonId;
}
