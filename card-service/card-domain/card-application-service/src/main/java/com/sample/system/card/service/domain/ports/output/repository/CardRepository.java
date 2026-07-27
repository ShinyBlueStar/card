package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.Card;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface for Card operations
 * Following DDD repository pattern
 */
public interface CardRepository {

    /**
     * Save a card
     */
    Card save(Card card);

    /**
     * Find card by ID
     */
    Optional<Card> findById(Long cardId);

    /**
     * Find card by PAN (card number)
     */
    Optional<Card> findByPan(String pan);

    List<Card> findAll();

    /**
     * Find all cards with pagination and filtering
     */
    Page<Card> findAllCards(Map<String, String> mapParameter);

    boolean existsByCardNumber(String cardNumber);

    long count();

    boolean existsByProfileId(@NotNull(message = "Card profile ID is required") Long cardProfileId);

    List<Card> findPrintableCards();
}
