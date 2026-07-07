package com.sample.system.card.service.domain.response.profile;

public record UpdateCardProfileResponse(
        Long cardProfileId,
        String message,
        Boolean isActive) {
}
