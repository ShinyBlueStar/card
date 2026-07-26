package com.sample.system.card.service.domain.event.reason;

import com.sample.system.card.service.domain.entity.Reason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReasonCreatedEvent {
    private final Reason reason;
    private final ZonedDateTime createdAt;
}

