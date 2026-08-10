package com.sample.system.card.service.domain.command.cardRequest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateRenewRequestCommand {
    @NotNull(message = "Card ID is required")
    private final Long cardId;
    @NotNull(message = "National ID is required")
    private final String nationalId;
    @NotNull(message = "Issuer Person ID is required")
    private final String issuerPersonId;
    @NotNull(message = "Unit ID is required")
    private final String unitId;
}

