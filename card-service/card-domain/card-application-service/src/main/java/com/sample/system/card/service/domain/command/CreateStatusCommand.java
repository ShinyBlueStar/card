package com.sample.system.card.service.domain.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for creating a new status
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateStatusCommand {

    @NotNull(message = "Code is required")
    private final String code;

    @NotNull(message = "Description is required")
    private final String description;

    private final String persianDescription;
}
