package com.sample.system.card.service.domain.command.feeProfile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetFeeProfileQuery {
    @NotNull(message = "FeeProfile ID is required")
    private final Long feeProfileId;
}

