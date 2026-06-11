package com.sample.system.card.service.domain.command.bank;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Command to change bank activation status.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeBankStatusCommand {

    @NotNull(message = "Bank id is required")
    private Long bankId;

    @NotNull(message = "Active flag is required")
    private Boolean active;
}

