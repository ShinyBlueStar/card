package com.sample.system.card.service.domain.response.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BatchProcessCardRequestsResponse {
    private final String hashId;
    private final int totalRequestIds;
    private final int validRequestsCount;
    private final int invalidRequestsCount;
    private final List<Long> processedRequestIds;
    private final List<Long> invalidRequestIds;
    private final String message;
}

