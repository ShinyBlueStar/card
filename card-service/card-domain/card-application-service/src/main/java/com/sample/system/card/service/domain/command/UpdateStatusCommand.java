package com.sample.system.card.service.domain.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateStatusCommand {

    @NotNull(message = "Code is required")
    private final String code;

    @NotNull(message = "Persian description is required")
    private final String persianDescription;
}
