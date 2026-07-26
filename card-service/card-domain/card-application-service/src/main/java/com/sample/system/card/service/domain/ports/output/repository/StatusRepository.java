package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.Status;
import com.sample.system.card.service.domain.exception.CardDomainException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface for Status operations
 * Following DDD repository pattern
 */
public interface StatusRepository {

    /**
     * Save a status
     */
    Status save(Status status);

    /**
     * Find status by ID
     */
    Optional<Status> findById(Long id);

    /**
     * Find status by code
     */
    Status findByCode(String code) throws CardDomainException;

    /**
     * Find all statuses
     */
    List<Status> findAll();

    /**
     * Check if status exists by code
     */

    /**
     * Count total statuses
     */
    long count();

    /**
     * Delete status by ID
     */
    void deleteById(Long id);

    /**
     * Find all statuses with search criteria
     */
    Page<Status> findAllStatuses(Map<String, String> mapParameter);
}
