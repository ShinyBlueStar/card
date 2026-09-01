package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.enums.Channel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Session domain model for card operations
 * Stored in Redis with cardId, channel, and expiration time
 */
@Getter
@Setter
@Builder
public class Session {
    private UUID sessionId;
    private UUID cardId;
    private Channel channel;
    private String sessionType;
    private String clientFingerprint;
    private Instant createdAt;
    private Instant expireAt;
    private boolean active;
}
