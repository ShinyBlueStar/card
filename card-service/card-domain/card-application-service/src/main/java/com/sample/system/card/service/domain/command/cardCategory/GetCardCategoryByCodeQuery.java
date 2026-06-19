package com.sample.system.card.service.domain.command.cardCategory;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Query for getting a card category by code
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class GetCardCategoryByCodeQuery {

    @NotBlank(message = "Category code is required")
    private final String code;
}
