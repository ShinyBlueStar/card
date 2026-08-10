package com.sample.system.card.service.domain.response.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateCardRequestResponse  {
    @NotNull
    private final Long cardRequestId;
}