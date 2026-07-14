package com.sample.system.card.service.domain.event.feeProfile;

import com.sample.system.card.service.domain.entity.FeeProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class FeeProfileCreatedEvent {
    private final FeeProfile feeProfile;
    private final ZonedDateTime createdAt;
}

