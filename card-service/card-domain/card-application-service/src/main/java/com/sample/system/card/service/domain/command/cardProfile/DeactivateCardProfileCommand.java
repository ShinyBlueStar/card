package com.sample.system.card.service.domain.command.cardProfile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for deactivating a card profile
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class DeactivateCardProfileCommand {

    @NotNull(message = "Card profile ID is required")
    private final Long cardProfileId;

    @NotNull(message = "Deactivation reason is required")
    private final String deactivationReason;

    private final String deactivatedBy;
    private final String notes;

    /**
     * Factory method for simple deactivation
     */
    public static DeactivateCardProfileCommand of(Long cardProfileId, String deactivationReason) {
        return DeactivateCardProfileCommand.builder()
                .cardProfileId(cardProfileId)
                .deactivationReason(deactivationReason)
                .build();
    }

}
