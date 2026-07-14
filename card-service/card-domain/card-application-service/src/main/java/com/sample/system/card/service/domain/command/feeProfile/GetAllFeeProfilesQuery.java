package com.sample.system.card.service.domain.command.feeProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetAllFeeProfilesQuery {
    private final Boolean activeOnly;

    public GetAllFeeProfilesQuery() {
        this.activeOnly = false;
    }
}

