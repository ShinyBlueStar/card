package com.sample.system.card.service.domain.command.bank;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ActivateBankCommand {
    @NotNull(message = "Bank ID is required")
    private final Long bankId;
}