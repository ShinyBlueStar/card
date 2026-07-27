package com.sample.system.card.service.domain.command.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command to deactivate a card
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class DeactivateCardCommand {
    @NotNull(message = "Card ID is required")
    private final Long cardId;

    @NotBlank(message = "Deactivation reason is required")
    private final Long reasonId;

    @NotBlank(message = "User who deactivates the card is required")
    private final String deactivatedBy;
}