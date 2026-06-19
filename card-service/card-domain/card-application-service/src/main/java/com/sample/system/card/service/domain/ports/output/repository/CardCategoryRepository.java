package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.CardCategory;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface for CardCategory operations
 * Following DDD repository pattern
 */
public interface CardCategoryRepository {

    /**
     * Save a card category
     */
    CardCategory save(CardCategory cardCategory);

    /**
     * Find card category by ID
     */
    Optional<CardCategory> findById(Long id);

    /**
     * Find card category by code
     */
    Optional<CardCategory> findByCode(Integer code);

    /**
     * Find all active card categories
     */
    List<CardCategory> findActiveCardCategories();

    List<CardCategory> findAll();

    /**
     * Check if card category exists by code
     */
    boolean existsByCode(Integer code);

    /**
     * Count total card categories
     */
    long count();

    /**
     * Delete card category by ID
     */
    void deleteById(Long id);

    /**
     * Find all card categories with pagination and filtering
     */
    Page<CardCategory> findAllCategories(Map<String, String> mapParameter);
}
