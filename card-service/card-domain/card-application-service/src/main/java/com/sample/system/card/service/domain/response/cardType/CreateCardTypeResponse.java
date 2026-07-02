package com.sample.system.card.service.domain.response.cardType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Response for creating a new card type
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateCardTypeResponse {

    private final Long cardTypeId;
    private final String code;
    private final String name;
    private final String description;
    private final Boolean isActive;
    private final String message;
}
