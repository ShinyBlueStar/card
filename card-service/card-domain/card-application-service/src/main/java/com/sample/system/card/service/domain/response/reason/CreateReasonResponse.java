package com.sample.system.card.service.domain.response.reason;

import com.sample.system.card.service.domain.enums.ReasonGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Response for creating a new reason
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateReasonResponse {

    private final Long reasonId;
    private final Integer code;
    private final String title;
    private final String reason;
    private final ReasonGroup groupId;
    private final String message;
}

