package com.sample.system.card.service.domain.command.bank;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Query for getting a bank by BIN code
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class GetBankByBinCodeQuery {

    @NotBlank(message = "BIN code is required")
    @Pattern(regexp = "\\d{6}", message = "BIN code must be exactly 6 digits")
    private final String binCode;
}
