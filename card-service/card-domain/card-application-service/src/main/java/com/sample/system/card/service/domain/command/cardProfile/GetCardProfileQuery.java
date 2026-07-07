package com.sample.system.card.service.domain.command.cardProfile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Query command to get card profile information
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class GetCardProfileQuery {
    @NotNull
    private final Long cardProfileId;
}
