package com.sample.system.card.service.domain.command.bank;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for creating a new bank
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateBankCommand {

    @NotBlank(message = "BIN code is required")
    @Pattern(regexp = "\\d{6}", message = "BIN code must be exactly 6 digits")
    private final String binCode;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private final String name;

    private final Boolean isActive;
}
