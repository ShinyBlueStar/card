package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.Session;
import com.sample.system.card.service.domain.exception.CardDomainException;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Session operations (Redis + SSM session lifecycle).
 */
public interface SessionRepository {

    /**
     * Create session in SSM (POST /api/v1/ssm/sessions). Returns the sessionId to use in X-Session-Id for generate/validate.
     */
    UUID createSessionInSsm(UUID cardId) throws CardDomainException;

    /**
     * Invalidate session in SSM (POST /api/v1/ssm/sessions/{id}/invalidate).
     */
    void invalidateSessionInSsm(UUID sessionId);

    /**
     * Save session to Redis
     */
    void save(Session session);

    /**
     * Find session by sessionId
     */
    Optional<Session> findBySessionId(UUID sessionId);

    /**
     * Find active session by cardId (if any).
     */
    Optional<Session> findActiveByCardId(UUID cardId);

    /**
     * Check if session exists and is valid
     */
    boolean isValid(UUID sessionId);

    /**
     * Invalidate session (Redis)
     */
    void invalidate(UUID sessionId);

    /**
     * Delete expired sessions (cleanup job)
     */
    void deleteExpired();
}
