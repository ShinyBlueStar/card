package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.CardNumberPatternCommandEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for CardNumberPatternCommandEntity
 * Following DDD infrastructure patterns
 */
@Repository
public interface CardNumberPatternCommandJpaRepository extends JpaRepository<CardNumberPatternCommandEntity, Long>, JpaSpecificationExecutor<CardNumberPatternCommandEntity> {

    @Query(value = "SELECT * FROM card_number_ranges WHERE CARD_PROFILE_ID = :profileId", nativeQuery = true)
    List<CardNumberPatternCommandEntity> findByCardProfileId(@Param("profileId") Long profileId);

    @Query(value = "SELECT * FROM card_number_ranges WHERE PRODUCT_CODE = :productCode", nativeQuery = true)
    List<CardNumberPatternCommandEntity> findByProductCode(@Param("productCode") String productCode);

    @Query(value = """
    SELECT e.* FROM card_number_ranges e
    WHERE e.CARD_PROFILE_ID = :profileId
      AND (:excludeId IS NULL OR e.ID <> :excludeId)
      AND NOT (e.CARD_NUMBER_TO < :fromValue OR e.CARD_NUMBER_FROM > :toValue)
""", nativeQuery = true)
    List<CardNumberPatternCommandEntity> findOverlappingPatterns(
            @Param("excludeId") Long excludeId,
            @Param("profileId") Long profileId,
            @Param("fromValue") String fromValue,
            @Param("toValue") String toValue);

    @Query(value = """
    SELECT e.* FROM card_number_ranges e
    WHERE e.PRODUCT_CODE = :productCode
      AND (:excludeId IS NULL OR e.ID <> :excludeId)
      AND NOT (e.CARD_NUMBER_TO < :fromValue OR e.CARD_NUMBER_FROM > :toValue)
""", nativeQuery = true)
    List<CardNumberPatternCommandEntity> findOverlappingPatternsByProductCode(
            @Param("excludeId") Long excludeId,
            @Param("productCode") String productCode,
            @Param("fromValue") String fromValue,
            @Param("toValue") String toValue);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from CardNumberPatternCommandEntity p where p.id = :id")
    Optional<CardNumberPatternCommandEntity> findByIdForUpdate(@Param("id") Long id);
}
