package com.sample.system.card.service.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateStatusResponse {
    private final Long statusId;
    private final String code;
    private final String persianDescription;
    private final String message;
}
