package com.sample.system.card.service.domain.command.card;

import com.sample.system.card.service.domain.enums.CardStatusAction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeCardStatusCommand {

    @NotNull(message = "Card id is required")
    private Long cardId;

    @NotNull(message = "Action is required")
    private CardStatusAction action;

    /**
     * Deactivation reason id (required for both activate/BLOCK scenarios currently).
     */
    private Long reasonId;

    @jakarta.validation.constraints.AssertTrue(
            message = "ReasonId is required when action is BLOCK, DEACTIVATE, or UNBLOCK"
    )
    private boolean isReasonValid() {
        if (action == null) return true;

        return switch (action) {
            case ACTIVATE -> true;
            case BLOCK, DEACTIVATE, UNBLOCK -> reasonId != null;
        };
    }
}

