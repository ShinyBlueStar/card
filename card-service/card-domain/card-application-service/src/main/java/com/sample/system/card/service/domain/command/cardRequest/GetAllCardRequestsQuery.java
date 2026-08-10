package com.sample.system.card.service.domain.command.cardRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Query for retrieving all card requests with optional filters
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class GetAllCardRequestsQuery {

    private final String status;
    private final String customerNumber;
    private final Long cardProfileId;
    private final String nationalId;

    /**
     * Default constructor for getting all card requests
     */
    public GetAllCardRequestsQuery() {
        this.status = null;
        this.customerNumber = null;
        this.cardProfileId = null;
        this.nationalId = null;
    }
}
