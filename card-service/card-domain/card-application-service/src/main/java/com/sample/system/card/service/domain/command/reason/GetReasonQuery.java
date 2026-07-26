package com.sample.system.card.service.domain.command.reason;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetReasonQuery {
    @NotNull(message = "Reason ID is required")
    private final Long reasonId;
}

