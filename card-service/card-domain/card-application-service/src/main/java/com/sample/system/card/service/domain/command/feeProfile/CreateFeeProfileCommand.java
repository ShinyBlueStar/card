package com.sample.system.card.service.domain.command.feeProfile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for creating a new fee profile
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateFeeProfileCommand {

    @NotBlank(message = "Fee profile name is required")
    @Size(max = 100, message = "Fee profile name must not exceed 100 characters")
    private final String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private final String description;

    private final Boolean isActive;
}

