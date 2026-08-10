package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.command.cardRequest.*;
import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.event.cardRequest.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.response.request.BatchProcessCardRequestsResponse;
import com.sample.system.card.service.domain.response.request.GenerateAccessFileForRequestResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface CardRequestService extends BaseService {

    /**
     * Create a new card request (Command)
     */
    CardRequestCreatedEvent createIssueCardRequest(CardRequest request) throws CardDomainException;

    /**
     * Get a specific card request by ID (Query)
     */
    CardRequest getCardRequest(CardRequest request) throws CardDomainException;

    /**
     * Get all card requests with optional filters (Query)
     */
    List<CardRequest> getAllCardRequests(GetAllCardRequestsQuery query) throws CardDomainException;

    /**
     * List card requests with pagination and filtering (Query)
     */
    Page<CardRequest> listCardRequests(Map<String, String> map, String caller, String ip) throws CardDomainException;

    /**
     * Update card request status (Command)
     */
    CardRequestUpdatedEvent updateCardRequestStatus(CardRequest request) throws CardDomainException;

    /**
     * Cancel a card request (Command) - لغو درخواست
     */
    CardRequestCancelledEvent cancelCardRequest(Long cardRequestId) throws CardDomainException;

    /**
     * Issue a card for an approved card request
     */
    CardRequestCreatedEvent issueCard(CardRequest request) throws CardDomainException;

    CardRequestCreatedEvent replaceCard(CardRequest request) throws CardDomainException;      // صدور کارت جديد
    CardRequestCreatedEvent reissueCard(CardRequest request) throws CardDomainException;  // صدور مجدد
    CardRequestCreatedEvent renewCard(CardRequest request) throws CardDomainException; //تمدید کارت

    CardRequestCreatedEvent createReplaceCardRequest(CardRequest request) throws CardDomainException;

    CardRequestCreatedEvent createRenewCardRequest(CardRequest request) throws CardDomainException;

    CardRequestProcessedEvent processCardRequest(CardRequest request) throws CardDomainException;

    GenerateAccessFileForRequestResponse generateAccessFileForRequest(Long requestId) throws CardDomainException;

    BatchProcessCardRequestsResponse batchProcessCardRequests(List<Long> requestIds) throws CardDomainException;
}
