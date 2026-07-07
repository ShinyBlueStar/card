package com.sample.system.card.service.domain.command.cardProfile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for activating a card profile
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class ActivateCardProfileCommand {

    @NotNull(message = "Card profile ID is required")
    private final Long cardProfileId;

    @NotNull(message = "Activation reason is required")
    private final String activationReason;

    private final String activatedBy;
    private final String notes;

    /**
     * Factory method for simple activation
     */
    public static ActivateCardProfileCommand of(Long cardProfileId, String activationReason) {
        return ActivateCardProfileCommand.builder()
                .cardProfileId(cardProfileId)
                .activationReason(activationReason)
                .build();
    }

}
