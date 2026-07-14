package com.sample.system.card.service.domain.response.feeProfile;

public record FeeProfileResponse(
        Long id,
        String name,
        String description,
        Boolean isActive,
        String feeType,
        String calculationMethod) {
}
