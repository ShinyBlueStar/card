package com.sample.system.card.service.domain.command.cardType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for creating a new card type
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateCardTypeCommand {

    @NotBlank(message = "Card type code is required")
    @Size(max = 30, message = "Card type code must not exceed 30 characters")
    private final String code;

    @NotBlank(message = "Card type name is required")
    @Size(max = 100, message = "Card type name must not exceed 100 characters")
    private final String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private final String description;

    private final Boolean isActive;
}
