package com.sample.system.card.service.domain.response.profile;

import jakarta.validation.constraints.NotNull;

public record CreateCardProfileResponse(
        @NotNull Long cardProfileId,
        @NotNull String message) {
}
