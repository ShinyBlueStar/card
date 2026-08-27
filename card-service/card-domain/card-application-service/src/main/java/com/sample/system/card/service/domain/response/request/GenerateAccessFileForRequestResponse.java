package com.sample.system.card.service.domain.response.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GenerateAccessFileForRequestResponse {
    private final Long requestId;
    private final String hashId;
    private final String message;
}

