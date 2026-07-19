package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.command.numberPattern.UpdateCardNumberPatternCommand;
import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.response.number.UpdateCardNumberPatternResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface for CardNumberPattern operations
 * Following DDD repository pattern
 */
public interface CardNumberPatternRepository {

    /**
     * Save a card number pattern
     */
    CardNumberPattern save(CardNumberPattern cardNumberPattern);

    /**
     * Find card number pattern by ID
     */
    Optional<CardNumberPattern> findById(Long id);

    List<CardNumberPattern> saveAll(List<CardNumberPattern> cardNumberPatterns);

    List<CardNumberPattern> findByCardProfileId(Long cardProfileId);

    List<CardNumberPattern> findByProductCode(String productCode);

    List<CardNumberPattern> findOverlappingPatterns(Long excludeId, Long profileId, String from, String to);

    List<CardNumberPattern> findOverlappingPatternsByProductCode(Long excludeId, String productCode, String from, String to);

    List<UpdateCardNumberPatternResponse> updateAll(
            @Valid List<UpdateCardNumberPatternCommand> commands);

    List<CardNumberPattern> findAll();

    /**
     * Find all patterns with pagination and filtering
     */
    Page<CardNumberPattern> findAll(Map<String, String> searchCriteria);

    /**
     * Count total patterns
     */
    long count();

    /**
     * Delete pattern by ID
     */
    void deleteById(Long id);

    /**
     * Check if pattern exists by ID
     */
    boolean existsById(Long id);

    Optional<CardNumberPattern> findByIdForUpdate(Long id);
}
