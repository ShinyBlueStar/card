package com.sample.system.card.service.domain.command.cardRequest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Command for creating a new card request
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class CreateIssueRequestCommand {

    @NotNull(message = "National ID is required")
    private final String nationalId;

    @NotNull(message = "Case Number is required")
    private final String caseNumber;

    @NotNull(message = "Card profile ID is required")
    private final Long cardProfileId;

    @NotNull(message = "Unit code is required")
    private final String unitCode;

    @NotNull(message = "Unit name is required")
    private final String unitName;

    @NotNull(message = "First name is required")
    private final String firstName;

    @NotNull(message = "Last name is required")
    private final String lastName;

    @NotNull(message = "Person ID is required")
    private final String issuerPersonId;
}