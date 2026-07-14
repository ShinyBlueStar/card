package com.sample.system.card.service.domain.response.feeProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Response for creating a new fee profile
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateFeeProfileResponse {

    private final Long feeProfileId;
    private final String name;
    private final String description;
    private final Boolean isActive;
    private final String message;
}

