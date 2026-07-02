package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.CardTypeCategory;
import com.sample.system.card.service.domain.valueObject.CardTypeCategoryId;
import com.sample.system.card.service.domain.valueObject.CardTypeId;
import com.sample.system.card.service.domain.valueObject.CardCategoryId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CardTypeCategory operations
 * Following DDD repository pattern
 */
public interface CardTypeCategoryRepository {

    /**
     * Save a card type category relationship
     */
    CardTypeCategory save(CardTypeCategory cardTypeCategory);

    /**
     * Find card type category by ID
     */
    Optional<CardTypeCategory> findById(CardTypeCategoryId cardTypeCategoryId);

    /**
     * Find card type category by card type and card category
     */
    Optional<CardTypeCategory> findByCardTypeAndCardCategory(Long cardTypeId, Long cardCategoryId);

    /**
     * Find all active card type categories
     */
    List<CardTypeCategory> findActiveCardTypeCategories();

    /**
     * Find card type categories by card type
     */
    List<CardTypeCategory> findByCardType(CardTypeId cardTypeId);

    /**
     * Find card type categories by card category
     */
    List<CardTypeCategory> findByCardCategory(CardCategoryId cardCategoryId);

    /**
     * Find all card type categories
     */
    List<CardTypeCategory> findAll();

    /**
     * Check if card type category exists by card type and card category
     */
    boolean existsByCardTypeAndCardCategory(CardTypeId cardTypeId, CardCategoryId cardCategoryId);

    /**
     * Count total card type categories
     */
    long count();

    /**
     * Delete card type category by ID
     */
    void deleteById(CardTypeCategoryId cardTypeCategoryId);
}
