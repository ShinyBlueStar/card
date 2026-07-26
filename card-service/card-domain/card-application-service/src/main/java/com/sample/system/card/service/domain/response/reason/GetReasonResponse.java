package com.sample.system.card.service.domain.response.reason;

import com.sample.system.card.service.domain.enums.ReasonGroup;

public record GetReasonResponse(
        Long id,
        Integer code,
        String title,
        String reason,
        ReasonGroup groupId) {
}

