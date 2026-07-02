package com.sample.system.card.service.domain.command.cardType;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Query for getting a card type by code
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class GetCardTypeByCodeQuery {

    @NotBlank(message = "Card type code is required")
    private final String code;
}
