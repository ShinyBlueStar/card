package com.sample.system.card.service.domain.command.card;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command to block a card
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class BlockCardCommand {
    @NotNull
    private final Long cardId;
    @NotNull(message = "دلیل مسدودسازی الزامی است")
    private final Long reasonId;
    private final String blockedBy;
}
