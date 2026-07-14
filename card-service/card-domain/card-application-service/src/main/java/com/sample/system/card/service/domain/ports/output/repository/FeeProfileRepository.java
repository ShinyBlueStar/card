package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.FeeProfile;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface for FeeProfile operations
 * Following DDD repository pattern
 */
public interface FeeProfileRepository {

    /**
     * Save a fee profile
     */
    FeeProfile save(FeeProfile feeProfile);

    /**
     * Find fee profile by ID
     */
    Optional<FeeProfile> findById(Long id);

    /**
     * Find fee profile by name
     */
    Optional<FeeProfile> findByName(String name);

    /**
     * Find all fee profiles
     */
    List<FeeProfile> findAll();

    /**
     * Find active fee profiles
     */
    List<FeeProfile> findActiveFeeProfiles();

    /**
     * Count total fee profiles
     */
    long count();

    /**
     * Check if fee profile exists by name
     */
    boolean existsByName(String name);

    /**
     * Find all fee profiles with pagination and filtering
     */
    Page<FeeProfile> findAllProfiles(Map<String, String> mapParameter);
}
