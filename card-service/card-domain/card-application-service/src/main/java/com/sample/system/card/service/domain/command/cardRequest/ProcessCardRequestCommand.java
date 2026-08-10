package com.sample.system.card.service.domain.command.cardRequest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command to process a card request
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class ProcessCardRequestCommand {
    @NotNull
    private final Long cardRequestId;
    @NotNull
    private final Integer requestType;
}
