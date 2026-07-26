package com.sample.system.card.service.domain.command.reason;

import com.sample.system.card.service.domain.enums.ReasonGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command for creating a new reason
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateReasonCommand {

    @NotNull(message = "Reason code is required")
    private final Integer code;

    @NotBlank(message = "Reason title is required")
    @Size(max = 300, message = "Reason title must not exceed 300 characters")
    private final String title;

    @Size(max = 300, message = "Reason description must not exceed 300 characters")
    private final String reason;

    @NotNull(message = "Reason group is required")
    private final ReasonGroup groupId;
}

