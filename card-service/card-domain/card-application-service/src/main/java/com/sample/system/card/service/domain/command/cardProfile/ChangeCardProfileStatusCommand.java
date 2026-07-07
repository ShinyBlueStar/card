package com.sample.system.card.service.domain.command.cardProfile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Command to change card profile activation status.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeCardProfileStatusCommand {

    @NotNull(message = "Card profile id is required")
    private Long cardProfileId;

    @NotNull(message = "Active flag is required")
    private Boolean active;
}

