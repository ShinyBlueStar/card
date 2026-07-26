package com.sample.system.card.service.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO for status information
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class GetStatusResponse  {
    private final Long id;
    private final String code;
    private final String description;
    private final String persianDescription;
    private final String message;
}
