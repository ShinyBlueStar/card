package com.sample.system.card.service.domain.command.numberPattern;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Command to deactivate a card number pattern
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class DeactivateCardNumberPatternCommand {
    @NotNull(message = "شناسه الگوی شماره کارت الزامی است")
    private final Long patternId;
}

