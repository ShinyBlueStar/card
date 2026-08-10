package com.sample.system.card.service.domain.command.cardRequest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Query command to get card request information
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class GetCardRequestQuery {
    @NotNull
    private final Long cardRequestId;
}
