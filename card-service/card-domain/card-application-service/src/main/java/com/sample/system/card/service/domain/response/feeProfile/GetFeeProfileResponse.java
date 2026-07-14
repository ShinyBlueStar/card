package com.sample.system.card.service.domain.response.feeProfile;

public record GetFeeProfileResponse(
        Long id,
        String name,
        String description,
        Boolean isActive) {
}

