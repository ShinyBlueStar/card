package com.sample.system.card.service.domain.response.number;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
/**
 * Response DTO for card number pattern information
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateCardNumberPatternResponse  {
    @NotNull
    private final Long cardNumberPatternId;
    @NotNull
    private final String message;
}

