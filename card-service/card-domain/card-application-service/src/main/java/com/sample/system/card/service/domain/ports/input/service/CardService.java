package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.command.card.GetAllCardsQuery;
import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.event.card.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.response.accessfile.AccessFileResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * Card Application Service Interface
 * Defines operations for Card management
 * Following Hexagonal Architecture - Input Port
 */
public interface CardService extends BaseService {

    /**
     * Create a new card (Command)
     */
    CardCreatedEvent createCard(Card card) throws CardDomainException;

    /**
     * Get a card by ID (Query)
     */
    Card getCard(Long id) throws CardDomainException;

    /**
     * Get all cards (Query)
     */
    List<Card> getAllCards(GetAllCardsQuery query) throws CardDomainException;

    /**
     * List cards with pagination and filtering (Query)
     */
    Page<Card> listCards(Map<String, String> map, String caller, String ip) throws CardDomainException;

    /**
     * Block a card (Command)
     */
    CardBlockedEvent blockCard(Card card, Long reasonId, String blockedBy) throws CardDomainException;

    /**
     * Unblock a card (Command) - رفع مسدودی
     */
    CardUnblockedEvent unblockCard(Card card, Long reasonId, String unblockedBy) throws CardDomainException;

    /**
     * Activate a card (Command)
     */
    CardActivatedEvent activateCard(Long cardId, Long reasonId) throws CardDomainException;

    /**
     * Deactivate a card (Command)
     */
    CardDeactivatedEvent deactivateCard(Long cardId, Long reasonId, String deactivatedBy) throws CardDomainException;

    /**
     * Replace an existing card with a newly generated card (new PAN)
     */
    CardCreatedEvent replaceCard(Long id, String personId) throws CardDomainException;
    void validateCardStatusForReplacement(Card card) throws CardDomainException;

    /**
     * Reissue a card by regenerating CVV2 and tracks (PAN remains the same)
     */
    CardCreatedEvent reissueCard(Card card) throws CardDomainException;

    /**
     * Renew a card by extending expiration date and regenerating CVV2 & tracks
     */
    CardCreatedEvent renewCard(Card card) throws CardDomainException;

    Boolean checkExistCardByCardNumber(String cardNumber) throws CardDomainException;

    /**
     * Check if any card exists for the given profile ID
     */
    Boolean existsByProfileId(Long cardProfileId) throws CardDomainException;

    AccessFileResponse generateAccessFile() throws CardDomainException;
}
