package com.sample.system.card.service.domain.command.numberPattern;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Query for getting a card number pattern by ID
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class GetCardNumberPatternQuery {

    @NotNull(message = "Pattern ID is required")
    private final Long patternId;
}
