package com.sample.system.card.service.domain.command.cardRequest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for creating a new card request
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateReplacementRequestCommand {

    @NotNull(message = "Card ID is required")
    private final Long cardId;
    @NotNull(message = "National ID is required")
    private final String nationalId;
    @NotNull(message = "Issuer Person ID is required")
    private final String issuerPersonId;
    @NotNull(message = "Unit ID is required")
    private final String unitId;
    @NotNull(message = "Reason ID is required")
    private final Long reasonId;
}