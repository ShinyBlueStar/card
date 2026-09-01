package com.sample.system.card.service.dataaccess.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.system.card.service.dataaccess.thirdparty.ThirdPartyException;
import com.sample.system.card.service.dataaccess.thirdparty.ssm.SsmChannel;
import com.sample.system.card.service.domain.entity.Session;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepository {

    private static final String REDIS_SESSION_KEY_PREFIX = "card:session:";
    private static final String REDIS_CARD_SESSION_KEY_PREFIX = "card:session:card:";
    private static final long SESSION_TTL_SECONDS = 1800; // 30 minutes default TTL

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final SsmChannel ssmChannel;

    @Override
    public UUID createSessionInSsm(UUID cardId) throws CardDomainException {
        try {
            log.info("SessionRepositoryImpl.createSessionInSsm: creating session in SSM for cardId={}", cardId);
            UUID sessionId = ssmChannel.createSession(cardId);
            log.info("SessionRepositoryImpl.createSessionInSsm: SSM returned sessionId={}", sessionId);
            return sessionId;
        } catch (ThirdPartyException e) {
            log.error("SSM createSession failed for cardId={}, httpStatus={}", cardId, e.getHttpStatusCode(), e);
            throw new CardDomainException(
                    "SSM session creation failed: " + (e.getChannelMessage() != null ? e.getChannelMessage() : e.getMessage()),
                    StatusService.SSM_SERVICE_ERROR,
                    HttpStatus.BAD_GATEWAY);
        }
    }

    @Override
    public void invalidateSessionInSsm(UUID sessionId) {
        if (sessionId == null) return;
        try {
            ssmChannel.invalidateSession(sessionId);
            log.debug("SessionRepositoryImpl.invalidateSessionInSsm: SSM session invalidated, sessionId={}", sessionId);
        } catch (Exception e) {
            log.warn("SSM invalidateSession failed (non-fatal), sessionId={}: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public void save(Session session) {
        if (session == null || session.getSessionId() == null) {
            log.warn("Attempted to save null session or session with null sessionId");
            return;
        }

        try {
            String key = REDIS_SESSION_KEY_PREFIX + session.getSessionId();
            String json = objectMapper.writeValueAsString(session);

            RBucket<String> bucket = redissonClient.getBucket(key);

            // Calculate TTL based on expireAt
            long ttlSeconds = SESSION_TTL_SECONDS;
            if (session.getExpireAt() != null) {
                long remainingSeconds = Duration.between(Instant.now(), session.getExpireAt()).getSeconds();
                if (remainingSeconds > 0) {
                    ttlSeconds = remainingSeconds;
                }
            }

            bucket.set(json, Duration.ofSeconds(ttlSeconds));

            // Also map cardId -> sessionId for quick lookup by cardId
            if (session.getCardId() != null) {
                String cardKey = REDIS_CARD_SESSION_KEY_PREFIX + session.getCardId();
                RBucket<String> cardBucket = redissonClient.getBucket(cardKey);
                cardBucket.set(session.getSessionId().toString(), Duration.ofSeconds(ttlSeconds));
            }

            log.debug("Session saved to Redis: sessionId={}, cardId={}, ttl={}s",
                    session.getSessionId(), session.getCardId(), ttlSeconds);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize session to JSON: sessionId={}", session.getSessionId(), e);
            throw new RuntimeException("Failed to save session", e);
        } catch (Exception e) {
            log.error("Error saving session to Redis: sessionId={}", session.getSessionId(), e);
            throw new RuntimeException("Failed to save session", e);
        }
    }

    @Override
    public Optional<Session> findBySessionId(UUID sessionId) {
        if (sessionId == null) {
            return Optional.empty();
        }

        try {
            String key = REDIS_SESSION_KEY_PREFIX + sessionId;
            RBucket<String> bucket = redissonClient.getBucket(key);
            String json = bucket.get();

            if (json == null || json.isBlank()) {
                log.debug("Session not found in Redis: sessionId={}", sessionId);
                return Optional.empty();
            }

            Session session = objectMapper.readValue(json, Session.class);
            log.debug("Session retrieved from Redis: sessionId={}, cardId={}", sessionId, session.getCardId());
            return Optional.of(session);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize session from JSON: sessionId={}", sessionId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error retrieving session from Redis: sessionId={}", sessionId, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isValid(UUID sessionId) {
        Optional<Session> sessionOpt = findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            return false;
        }

        Session session = sessionOpt.get();

        // Check if session is active
        if (!session.isActive()) {
            log.debug("Session is not active: sessionId={}", sessionId);
            return false;
        }

        // Check expiration
        if (session.getExpireAt() != null && Instant.now().isAfter(session.getExpireAt())) {
            log.debug("Session expired: sessionId={}, expireAt={}", sessionId, session.getExpireAt());
            invalidate(sessionId);
            return false;
        }

        return true;
    }

    @Override
    public void invalidate(UUID sessionId) {
        if (sessionId == null) {
            return;
        }

        try {
            findBySessionId(sessionId)
                    .map(Session::getCardId)
                    .ifPresent(cardId -> {
                        redissonClient.getBucket(REDIS_CARD_SESSION_KEY_PREFIX + cardId).delete();
                        log.debug("Removed card->session mapping for cardId={}", cardId);
                    });
            String key = REDIS_SESSION_KEY_PREFIX + sessionId;
            RBucket<String> bucket = redissonClient.getBucket(key);
            bucket.delete();
            log.debug("Session invalidated: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("Error invalidating session in Redis: sessionId={}", sessionId, e);
        }
    }

    @Override
    public void deleteExpired() {
        // Redis TTL handles expiration automatically, but we can add cleanup logic here if needed
        log.debug("Expired session cleanup - Redis TTL handles expiration automatically");
    }

    @Override
    public Optional<Session> findActiveByCardId(UUID cardId) {
        if (cardId == null) {
            return Optional.empty();
        }

        try {
            String cardKey = REDIS_CARD_SESSION_KEY_PREFIX + cardId;
            RBucket<String> cardBucket = redissonClient.getBucket(cardKey);
            String sessionIdStr = cardBucket.get();

            if (sessionIdStr == null || sessionIdStr.isBlank()) {
                log.debug("No session mapping found for cardId={}", cardId);
                return Optional.empty();
            }

            UUID sessionId = UUID.fromString(sessionIdStr);

            if (!isValid(sessionId)) {
                log.debug("Mapped session is not valid for cardId={}, sessionId={}", cardId, sessionId);
                return Optional.empty();
            }

            return findBySessionId(sessionId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid sessionId format stored for cardId={}", cardId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error finding active session by cardId={}", cardId, e);
            return Optional.empty();
        }
    }
}
