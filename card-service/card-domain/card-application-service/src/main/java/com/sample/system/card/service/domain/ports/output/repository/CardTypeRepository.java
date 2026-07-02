package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.CardType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface for CardType operations
 * Following DDD repository pattern
 */
public interface CardTypeRepository {

    /**
     * Save a card type
     */
    CardType save(CardType cardType);

    /**
     * Find card type by ID
     */
    Optional<CardType> findById(Long id);

    /**
     * Find card type by code
     */
    Optional<CardType> findByCode(Integer code);

    /**
     * Find all active card types
     */
    List<CardType> findActiveCardTypes();

    /**
     * Find all card types
     */
    List<CardType> findAll();

    /**
     * Check if card type exists by code
     */
    boolean existsByCode(Integer code);

    /**
     * Count total card types
     */
    long count();

    /**
     * Delete card type by ID
     */
    void deleteById(Long id);

    /**
     * Find all card types with pagination and filtering
     */
    Page<CardType> findAllTypes(Map<String, String> mapParameter);
}
