package com.sample.system.card.service.domain.command.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Query for retrieving all cards
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class GetAllCardsQuery {

    private final Boolean activeOnly;
    private final String customerNumber;

    /**
     * Default constructor for getting all cards
     */
    public GetAllCardsQuery() {
        this.activeOnly = false;
        this.customerNumber = null;
    }
}
