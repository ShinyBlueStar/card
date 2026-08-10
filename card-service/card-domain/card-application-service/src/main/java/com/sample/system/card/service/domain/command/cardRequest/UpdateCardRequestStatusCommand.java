package com.sample.system.card.service.domain.command.cardRequest;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Command to update card request status
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class UpdateCardRequestStatusCommand {
    @NotNull
    private final Long cardRequestId;
    @NotNull
    private final Integer newStatus;
}
