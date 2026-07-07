package com.sample.system.card.service.domain.command.cardProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Query for retrieving all card profiles
 * Following CQRS pattern for read operations
 */
@Getter
@Builder
@AllArgsConstructor
public class GetAllCardProfilesQuery {

    private final Boolean activeOnly;

    /**
     * Default constructor for getting all card profiles
     */
    public GetAllCardProfilesQuery() {
        this.activeOnly = false;
    }
}
