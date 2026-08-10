package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.enums.CardRequestStatus;
import com.sample.system.card.service.domain.enums.CardRequestType;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.valueObject.CardRequestId;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface for CardRequest operations
 * Following DDD repository pattern
 */
public interface CardRequestRepository {

    /**
     * Save a card request
     */
    CardRequest saveInitiateReq(CardRequest cardRequest) throws CardDomainException;

    CardRequest saveReplacementReq(CardRequest cardRequest) throws CardDomainException;
    CardRequest updateIssueReq(CardRequest cardRequest) throws CardDomainException;
    CardRequest save(CardRequest cardRequest);
    CardRequest updateStatus(CardRequest cardRequest) throws CardDomainException;

    /**
     * Find card request by ID
     */
    Optional<CardRequest> findById(Long cardRequestId);

    Optional<CardRequest> findByIdAndType(Long cardRequestId, CardRequestType requestType);
    List<CardRequest> findAll();

    /**
     * Find all card requests with pagination and filtering
     */
    Page<CardRequest> findAllCardRequests(Map<String, String> mapParameter);
    long count();

    /**
     * Delete card request by ID
     */
    void deleteById(CardRequestId cardRequestId);

    Boolean existByIdAndStatus(CardRequest request, CardRequestStatus status) throws CardDomainException;

    Boolean existByTypeAndStatusAndPan(CardRequestType type,String pan,List<Integer> statusList) throws CardDomainException;//toDo: status not in initial and Approved

    Boolean existByTypeAndStatusAndCardId(CardRequestType type, Long cardId, List<Integer> statusList) throws CardDomainException;

    Boolean findByNationalIdAndCaseNumberAndTypeAndStatus(
            String nationalId, String caseNumber, CardRequestType requestType);
}
