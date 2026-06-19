package com.sample.system.card.service.domain.command.cardCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for updating an existing card category
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class UpdateCardCategoryCommand {

    @NotNull(message = "Card category ID is required")
    private final Long cardCategoryId;

    @NotNull(message = "Card type ID is required")
    private final Long cardTypeId;

    @NotBlank(message = "Category code is required")
    @Size(max = 30, message = "Category code must not exceed 30 characters")
    private final String code;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private final String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private final String description;

    private final Boolean isActive;
}
