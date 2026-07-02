package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.CardTypeCategoryCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for CardTypeCategory operations
 * Following Repository pattern
 */
@Repository
public interface CardTypeCategoryJpaRepository extends JpaRepository<CardTypeCategoryCommandEntity, Long> {

    /**
     * Find card type category by card type and card category
     */
    @Query("SELECT ctc FROM CardTypeCategoryCommandEntity ctc WHERE ctc.cardType.id = :cardTypeId AND ctc.cardCategory.id = :cardCategoryId")
    Optional<CardTypeCategoryCommandEntity> findByCardTypeIdAndCardCategoryId(@Param("cardTypeId") Long cardTypeId, @Param("cardCategoryId") Long cardCategoryId);

    /**
     * Find all active card type categories
     */
    @Query("SELECT ctc FROM CardTypeCategoryCommandEntity ctc WHERE ctc.isActive = true")
    List<CardTypeCategoryCommandEntity> findActiveCardTypeCategories();

    /**
     * Find card type categories by card type
     */
    @Query("SELECT ctc FROM CardTypeCategoryCommandEntity ctc WHERE ctc.cardType.id = :cardTypeId")
    List<CardTypeCategoryCommandEntity> findByCardTypeId(@Param("cardTypeId") Long cardTypeId);

    /**
     * Find card type categories by card category
     */
    @Query("SELECT ctc FROM CardTypeCategoryCommandEntity ctc WHERE ctc.cardCategory.id = :cardCategoryId")
    List<CardTypeCategoryCommandEntity> findByCardCategoryId(@Param("cardCategoryId") Long cardCategoryId);

    /**
     * Check if card type category exists by card type and card category
     */
    @Query("SELECT COUNT(ctc) > 0 FROM CardTypeCategoryCommandEntity ctc WHERE ctc.cardType.id = :cardTypeId AND ctc.cardCategory.id = :cardCategoryId")
    boolean existsByCardTypeIdAndCardCategoryId(@Param("cardTypeId") Long cardTypeId, @Param("cardCategoryId") Long cardCategoryId);
}
