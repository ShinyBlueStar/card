package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.CardDomainService;
import com.sample.system.card.service.domain.command.cardRequest.*;
import com.sample.system.card.service.domain.entity.*;
import com.sample.system.card.service.domain.enums.CardIssueMethod;
import com.sample.system.card.service.domain.enums.CardRequestStatus;
import com.sample.system.card.service.domain.enums.CardRequestType;
import com.sample.system.card.service.domain.enums.CardStatus;
import com.sample.system.card.service.domain.event.cardRequest.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.CardProfileService;
import com.sample.system.card.service.domain.ports.input.service.CardRequestService;
import com.sample.system.card.service.domain.ports.input.service.CardService;
import com.sample.system.card.service.domain.ports.input.service.CustomerService;
import com.sample.system.card.service.domain.ports.input.service.ReasonService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.exporter.CardPrintExporterRepository;
import com.sample.system.card.service.domain.ports.output.repository.FileDownloadRepository;
import com.sample.system.card.service.domain.response.accessfile.result.AccessFileResult;
import com.sample.system.card.service.domain.response.request.GenerateAccessFileForRequestResponse;
import com.sample.system.card.service.domain.response.request.BatchProcessCardRequestsResponse;
import com.sample.system.card.service.domain.entity.FileDownload;
import com.sample.system.card.service.domain.event.card.CardCreatedEvent;
import com.sample.system.card.service.domain.utility.PanMaskingUtil;
import com.sample.system.card.service.domain.ports.output.repository.CardRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CardRequest Application Service Implementation
 * Handles CardRequest business operations
 * Following Hexagonal Architecture and DDD patterns
 */
@Slf4j
@Service
@RequiredArgsConstructor
class CardRequestServiceImpl implements CardRequestService {

    private final CardRequestRepository repository;
    private final CardDomainService cardDomainService;
    private final CardProfileService cardProfileService;
    private final CardService cardService;
    private final ReasonService reasonService;
    private final CustomerService customerService;
    private final CardPrintExporterRepository cardPrintExporter;
    private final FileDownloadRepository fileDownloadRepository;

    @Override
    public CardRequestCreatedEvent createIssueCardRequest(CardRequest request)
            throws CardDomainException {
        log.info("Starting Issue CardRequest creation service - NationalId: {}, CardProfileId: {}",
                request.getNationalId(), request.getCardProfileId());
        try {
            if (request.getNationalId() != null && request.getCaseNumber() != null) {
                Map<String, String> params = Map.of(
                        "nationalId", request.getNationalId(),
                        "caseNumber", request.getCaseNumber()
                );

                if (repository.findByNationalIdAndCaseNumberAndTypeAndStatus(
                        request.getNationalId(),
                        request.getCaseNumber(),
                        CardRequestType.CARD_ISSUANCE)) {
                    log.warn("Duplicate issue request found - NationalId: {}, CaseNumber: {}",
                            request.getNationalId(),
                            request.getCaseNumber());
                    throw new CardDomainException(
                            String.format("Duplicate request found for nationalId: %s and caseNumber: %s with type: %s and status: %s",
                                    request.getNationalId(), request.getCaseNumber(),
                                    CardRequestType.CARD_ISSUANCE, request.getRequestStatus()),
                            StatusService.DUPLICATE_REQUEST_FOR_NATIONALID_CASENUMBER,
                            HttpStatus.BAD_REQUEST, params);
                }
            }

            request.setRequestStatus(CardRequestStatus.INITIAL);
            Customer customer = customerService.createOrGetCustomerFromRequest(request);

            cardDomainService.validateAndInitiateCardRequest(request);
            if (request.getCardProfileId() != null && request.getCardProfileId().getValue() != null) {
                log.debug("Validating CardProfile existence - CardProfileId: {}", request.getCardProfileId().getValue());
                request.setCardProfile(
                        cardProfileService.findById(request.getCardProfileId().getValue()));
                log.debug("CardProfile validation completed successfully");
            }
            CardRequest savedRequest = repository.saveInitiateReq(request);
            checkNullRequest(savedRequest, request.getNationalId());
            CardRequestCreatedEvent event = new CardRequestCreatedEvent(savedRequest);
            log.info("CardRequest initiate process completed successfully - RequestId: {}, NationalId: {}",
                    savedRequest.getId().getValue(), savedRequest.getNationalId());
            return event;
        } catch (CardDomainException e) {
            log.error("Error occurred during CardRequest creation - NationalId: {}, Error: {}",
                    request.getNationalId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred during CardRequest creation - NationalId: {}, Error: {}",
                    request.getNationalId(), e.getMessage(), e);
            throw logAndBuildException(
                    "Unexpected error while creating issue card request",
                    "CREATE_ISSUE_REQUEST_ERROR",
                    StatusService.CREATE_ISSUE_REQUEST_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public CardRequest getCardRequest(CardRequest request) throws CardDomainException {
        Long requestId = request != null && request.getId() != null ? request.getId().getValue() : null;
        if (requestId == null) {
            throw logAndBuildException(
                    "CardRequest retrieval rejected because requestId is null",
                    "CardRequest ID is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }

        log.info("Starting CardRequest retrieval service - RequestId: {}", requestId);

        try {
            CardRequest foundRequest = repository.findById(requestId)
                    .orElseThrow(() -> logAndBuildException(
                            "CardRequest not found with id " + requestId,
                            "CardRequest not found: " + requestId,
                            StatusService.REQUEST_NOT_FOUND,
                            HttpStatus.NOT_FOUND));
            log.info("CardRequest retrieval completed successfully - RequestId: {}", requestId);
            return foundRequest;
        } catch (CardDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred in CardRequest retrieval service - RequestId: {}, Error: {}",
                    requestId, e.getMessage(), e);
            throw logAndBuildException(
                    "Error occurred while retrieving CardRequest with id " + requestId,
                    "Error occurred in CardRequest retrieval service",
                    StatusService.REQUEST_NOT_FOUND,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<CardRequest> getAllCardRequests(GetAllCardRequestsQuery query) throws CardDomainException {
        log.info("Starting CardRequests retrieval service - Status: {}, CustomerNumber: {}, CardProfileId: {}",
                query.getStatus(), query.getCustomerNumber(), query.getCardProfileId());
        List<CardRequest> allRequests = repository.findAll();
        List<CardRequest> filteredRequests = allRequests.stream()
                .filter(request -> {
                    // Filter by status if provided
                    if (query.getStatus() != null && !query.getStatus().isEmpty()) {
                        if (request.getRequestStatus() != null &&
                                !query.getStatus().equalsIgnoreCase(request.getRequestStatus().name())) {
                            return false;
                        }
                    }
                    // Filter by customer number (nationalId) if provided
                    if (query.getCustomerNumber() != null && !query.getCustomerNumber().isEmpty()) {
                        if (request.getNationalId() == null ||
                                !query.getCustomerNumber().equals(request.getNationalId())) {
                            return false;
                        }
                    }
                    // Filter by card profile ID if provided
                    if (query.getCardProfileId() != null) {
                        Long profileId = request.getCardProfileId() != null
                                ? request.getCardProfileId().getValue() : null;
                        if (!query.getCardProfileId().equals(profileId)) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();

        log.info("CardRequests retrieval completed successfully - Count: {}", filteredRequests.size());
        return filteredRequests;
    }

    @Override
    public CardRequestProcessedEvent processCardRequest(CardRequest requestWithType) throws CardDomainException {
        Long requestId = requestWithType.getId() != null ? requestWithType.getId().getValue() : null;
        if (requestId == null) {
            throw new CardDomainException(
                    "CardRequestId is required for processing",
                    StatusService.CARD_REQUEST_ID_REQUIRED,
                    HttpStatus.BAD_REQUEST);
        }

        CardRequestType requestedType = requestWithType.getRequestType();
        if (requestedType == null) {
            throw new CardDomainException(
                    "RequestType is required for processing",
                    StatusService.CARD_REQUEST_TYPE_REQUIRED,
                    HttpStatus.BAD_REQUEST);
        }

        log.info("Starting CardRequest processing service - RequestId: {}, RequestType: {}",
                requestId, requestedType);

        // Fetch request from repository with ID and Type
        CardRequest request = repository.findByIdAndType(requestId, requestedType)
                .orElseThrow(() -> new CardDomainException(
                        String.format("CardRequest not found with id %s and type %s", requestId, requestedType),
                        StatusService.REQUEST_NOT_FOUND,
                        HttpStatus.NOT_FOUND));

        // Check if request status is APPROVED
        if (!CardRequestStatus.APPROVED.equals(request.getRequestStatus())) {
            log.error("CardRequest status is not APPROVED - RequestId: {}, Status: {}",
                    requestId, request.getRequestStatus());
            throw new CardDomainException(
                    String.format("CardRequest status is not APPROVED. Current status: %s",
                            request.getRequestStatus() != null ? request.getRequestStatus() : "null"),
                    StatusService.CARD_REQUEST_STATUS_IS_INVALID,
                    HttpStatus.BAD_REQUEST);
        }

        log.info("CardRequest validation passed - RequestId: {}, Type: {}, Status: {}",
                requestId, requestedType, request.getRequestStatus());

        // Route to appropriate processor based on request type
        log.debug("Routing {} request to its processor - RequestId: {}", requestedType, requestId);
        CardRequestCreatedEvent processedEvent = switch (requestedType) {
            case CARD_ISSUANCE -> issueCard(request);
            case CARD_REPLACEMENT -> replaceCard(request);
            case CARD_RENEWAL -> renewCard(request); // تمدید
            case CARD_REISSUANCE -> reissueCard(request);
            default -> {
                log.error("Unsupported request type for processing - RequestId: {}, Type: {}",
                        requestId, requestedType);
                throw new CardDomainException(
                        "Unsupported request type for processing: " + requestedType,
                        StatusService.INPUT_PARAMETER_NOT_VALID,
                        HttpStatus.BAD_REQUEST);
            }
        };

        CardRequest processedRequest = processedEvent.getCardRequest();
        log.info("CardRequest status updated to FINISHED - RequestId: {}", processedRequest.getId().getValue());

        // Create processed event from updated request
        CardRequestProcessedEvent event = new CardRequestProcessedEvent(processedRequest);
        log.info("CardRequest processing completed successfully - RequestId: {}, Type: {}, Status: {}",
                processedRequest.getId().getValue(),
                requestedType,
                processedRequest.getRequestStatus());
        return event;
    }

    @Override
    public CardRequestUpdatedEvent updateCardRequestStatus(CardRequest request) throws CardDomainException {
        log.info("Starting CardRequest status update service - RequestId: {}, Status:{}" ,
                request.getId().getValue(), request.getRequestStatus());
        try {
            if (CardRequestStatus.APPROVED.equals(request.getRequestStatus())) {
                repository.existByIdAndStatus(request, CardRequestStatus.INITIAL);
            }else if(CardRequestStatus.PRINTED.equals(request.getRequestStatus()))
                repository.existByIdAndStatus(request, CardRequestStatus.PRINT_EXPECT);
            else
                throw new CardDomainException("CardRequest status now should be just APPROVED or PRINTED",
                        StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
            CardRequest updated = repository.updateStatus(request);
            CardRequestUpdatedEvent event = new CardRequestUpdatedEvent(updated);
            log.info("CardRequest status update process completed successfully - RequestId: {}", updated.getId().getValue());
            return event;
        } catch (Exception e) {
            log.error("Error occurred during CardRequest status update - RequestId: {}, Error: {}",
                    request.getId().getValue(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public CardRequestCancelledEvent cancelCardRequest(Long requestId) throws CardDomainException {
        log.info("Starting CardRequest cancellation service - RequestId: {}", requestId);
        CardRequest request = repository.findById(requestId)
                .orElseThrow(() -> new CardDomainException("CardRequest not found: " + requestId));
        if (CardRequestStatus.APPROVED == request.getRequestStatus() ||
                CardRequestStatus.CANCELED == request.getRequestStatus()) {
            throw new CardDomainException(String.format("request with current status:%s is not acceptable. RequestId:%s ",
                    request.getRequestStatus(),requestId),
                    StatusService.CARD_REQUEST_STATUS_IS_INVALID,
                    HttpStatus.BAD_REQUEST);
        }
        request.setRequestStatus(CardRequestStatus.CANCELED);
        CardRequest updated = repository.updateStatus(request);
        CardRequestCancelledEvent event = new CardRequestCancelledEvent(updated);
        log.info("CardRequest cancellation completed successfully - RequestId: {}, New Status: {}",
                updated.getId().getValue(), updated.getRequestStatus());
        return event;
    }

    @Override
    public Page<CardRequest> listCardRequests(Map<String, String> map, String caller, String ip) throws CardDomainException {
        log.info("Starting CardRequests listing service with pagination - Caller: {}, IP: {}", caller, ip);
        try {
            Page<CardRequest> cardRequestsPage = repository.findAllCardRequests(map);
            log.info("CardRequests listing completed successfully - Count: {}",
                    cardRequestsPage != null ? cardRequestsPage.getContent().size() : 0);
            return cardRequestsPage;
        } catch (Exception e) {
            log.error("Error occurred during CardRequests listing - Caller: {}, IP: {}, Error: {}",
                    caller, ip, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public CardRequestCreatedEvent issueCard(CardRequest lastRequest) throws CardDomainException {
        Long requestId = lastRequest.getId().getValue();
        log.info("Starting issueCard service - RequestId: {}", requestId);
        if (lastRequest.getCardProfileId().getValue() == null) {
            throw new CardDomainException("CardProfileId is required to issue card for card Issue - RequestId: {} " + requestId,
                    StatusService.CARD_PROFILE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Customer customer = customerService.createOrGetCustomerFromRequest(lastRequest);

        Card card = Card.builder()
                .caseNumber(lastRequest.getCaseNumber())
                .cardProfileId(lastRequest.getCardProfileId())
                .unitCode(lastRequest.getUnitCode())
                .unitName(lastRequest.getUnitName())
                .customer(customer)
                .issuerPersonId(lastRequest.getIssuerPersonId())
                .build();
        CardCreatedEvent cardCreatedEvent = cardService.createCard(card);
        lastRequest.setCard(cardCreatedEvent.getCard());
        lastRequest.setRequestStatus(CardRequestStatus.ISSUED);
        repository.updateIssueReq(lastRequest);
        log.info("issue card request updated successfully with Id: {} and new card was generated " +
                        "with card id: {}, card number: {} ", lastRequest.getId(),
                lastRequest.getCard().getId(),
                lastRequest.getCard().getMaskedPan() != null ? lastRequest.getCard().getMaskedPan() : PanMaskingUtil.maskPan(lastRequest.getCard().getPan()));
        return new CardRequestCreatedEvent(lastRequest, null, null);
    }

    @Override
    public CardRequestCreatedEvent replaceCard(CardRequest persistedRequest) throws CardDomainException {//المثنی
        Long requestId = persistedRequest.getId().getValue();
        log.info("Starting replaceCard request service - RequestId: {}", requestId);
        ensureApproved(persistedRequest, CardRequestType.CARD_REPLACEMENT);
        if (persistedRequest.getOldCardId() == null) {
            throw new CardDomainException(
                    "CardId is required to replace card",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }
        CardCreatedEvent newCardEvent = cardService.replaceCard(persistedRequest.getOldCardId(),
                persistedRequest.getIssuerPersonId());
        Card newCard = newCardEvent.getCard();

        if (newCard != null && newCard.getId() != null) {
            persistedRequest.setCardId(newCard.getId());
        }
        persistedRequest.setCard(newCard);
        log.info("SET STATUS OF REQUEST: {} TO FINISH",persistedRequest.getId());
        persistedRequest.setRequestStatus(CardRequestStatus.FINISHED);
        CardRequest savedRequest = repository.save(persistedRequest);
        log.info("replaceCard request service completed - RequestId: {}, NewCardId: {}",
                requestId,
                newCard != null && newCard.getId() != null ? newCard.getId().getValue() : null);
        return new CardRequestCreatedEvent(savedRequest);
    }

    @Override
    public CardRequestCreatedEvent reissueCard(CardRequest persistedRequest) throws CardDomainException {//صدور مجدد
        Long requestId = persistedRequest.getId().getValue();
        log.info("Starting reissueCard request service - RequestId: {}", requestId);

        ensureApproved(persistedRequest, CardRequestType.CARD_REISSUANCE);

        loadCard(persistedRequest.getCard());

        CardCreatedEvent newCardEvent = cardService.reissueCard(persistedRequest.getCard());
        Card newCard = newCardEvent.getCard();

        updateRequestWithNewCard(persistedRequest, newCard, CardRequestType.CARD_REISSUANCE);

        CardRequest savedRequest = repository.save(persistedRequest);
        log.info("reissueCard request service completed - RequestId: {}, NewCardId: {}",
                requestId,
                newCard != null && newCard.getId() != null ? newCard.getId().getValue() : null);
        return new CardRequestCreatedEvent(savedRequest);
    }

    @Override
    public CardRequestCreatedEvent renewCard(CardRequest persistedRequest) throws CardDomainException {//تمدید کارت
        Long requestId = persistedRequest.getId().getValue();
        log.info("Starting renewCard request service - RequestId: {}", requestId);

        ensureApproved(persistedRequest, CardRequestType.CARD_RENEWAL);

        Card originalCard = loadCard(persistedRequest.getCard());
        if (originalCard.getCardStatus() == CardStatus.HOT) {
            throw new CardDomainException("Cannot renew HOT card - CardId: " + originalCard.getId().getValue());
        }

        CardCreatedEvent newCardEvent = cardService.renewCard(persistedRequest.getCard());
        Card newCard = newCardEvent.getCard();

        updateRequestWithNewCard(persistedRequest, newCard, CardRequestType.CARD_RENEWAL);

        CardRequest savedRequest = repository.save(persistedRequest);
        log.info("renewCard request service completed - RequestId: {}, NewCardId: {}",
                requestId,
                newCard != null && newCard.getId() != null ? newCard.getId().getValue() : null);
        return new CardRequestCreatedEvent(savedRequest);
    }

    @Override
    public CardRequestCreatedEvent createReplaceCardRequest(CardRequest request) throws CardDomainException {
        log.info("Creating Replacement Card Request service - cardId: {}", request.getCardId());
        log.info("Validating request duplicate exist for replacement card");
        existByTypeAndStatusAndCardId(CardRequestType.CARD_REPLACEMENT, request.getOldCardId());

        // Check if card exists
        Card card = cardService.getCard(request.getOldCardId());
        cardService.validateCardStatusForReplacement(card);

        if (card.getCardProfileId() != null && card.getCardProfileId().getValue() != null) {
            request.setProfileId(card.getCardProfileId().getValue());
            log.debug("Extracted profileId {} from card {} for replacement request",
                    card.getCardProfileId().getValue(), request.getOldCardId());
        }

        Reason reason = reasonService.findById(request.getReasonId());
        request.setReason(reason);
        request.setReasonId(reason.getId() != null ? reason.getId().getValue() : request.getReasonId());
        request.setRequestStatus(CardRequestStatus.INITIAL);
        request.setRequestType(CardRequestType.CARD_REPLACEMENT);
        CardRequest savedRequest = repository.save(request);
        checkNullRequest(savedRequest, request.getNationalId());
        CardRequestCreatedEvent event = new CardRequestCreatedEvent(savedRequest);
        log.info("Replace Card Request initiate processed successfully" +
                        " - RequestId: {}, CardId: {}, Status: {}, ReasonId: {}",
                savedRequest.getId().getValue(),
                card != null ? card.getId().getValue() : null,
                card.getCardStatus(),
                request.getReasonId());
        return event;
    }

    @Override
    public CardRequestCreatedEvent createRenewCardRequest(CardRequest request) throws CardDomainException {
        log.info("Creating Renewal Card Request service - cardId: {}", request.getCardId());
        if (request.getCardId() == null || request.getCardId().getValue() == null) {
            throw logAndBuildException(
                    "CardId is required for renewal request",
                    "CardId is required for renewal request",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }
        //validate unitId

        // Check if card exists
        Card card = cardService.getCard(request.getCardId().getValue());
        request.setCard(card);
        if (card.getCardProfileId() != null && card.getCardProfileId().getValue() != null) {
            request.setProfileId(card.getCardProfileId().getValue());
            log.info("Extracted profileId {} from card {} for renewal request",
                    card.getCardProfileId().getValue(), request.getCardId().getValue());
        }
        // Check for duplicate requests
        existByTypeAndStatusAndCardId(CardRequestType.CARD_RENEWAL, request.getCardId().getValue());
        request.setRequestStatus(CardRequestStatus.INITIAL);
        request.setRequestType(CardRequestType.CARD_RENEWAL);
        cardDomainService.validateAndInitiateCardRequest(request);
        CardRequest savedRequest = repository.save(request);
        checkNullRequest(savedRequest, request.getNationalId());
        CardRequestCreatedEvent event = new CardRequestCreatedEvent(savedRequest);
        log.info("CardRequest initiate process completed successfully - RequestId: {}, NationalId: {}",
                savedRequest.getId().getValue(), savedRequest.getNationalId());
        return event;
    }

    private void checkNullRequest(CardRequest savedRequest, String nationalId) throws CardDomainException{
        if (savedRequest == null) {
            log.error("Could not save CardRequest for nationalId: {}, pan: {}", nationalId);
            throw logAndBuildException(
                    String.format("Repository returned null while saving issue card request for nationalId:(%s)",nationalId),
                    String.format("Could not save CardRequest for nationalId: %s ", nationalId),
                    StatusService.REQUEST_NOT_FOUND,
                    HttpStatus.OK);
        }
    }

    private Card loadCard(Card card) throws CardDomainException {
        Long cardId = requireCardId(card);
        return cardService.getCard(cardId);
    }

    private void ensureApproved(CardRequest request, CardRequestType fallbackType) throws CardDomainException {
        if (request.getRequestStatus() != CardRequestStatus.APPROVED) {
            Long requestId = request.getId() != null ? request.getId().getValue() : null;
            throw logAndBuildException(
                    "CardRequest must be approved before processing. RequestId=" + requestId,
                    "CardRequest must be APPROVED before processing. RequestId: " + requestId,
                    StatusService.CARD_REQUEST_STATUS_IS_INVALID,
                    HttpStatus.BAD_REQUEST);
        }
        if (request.getRequestType() == null) {
            request.setRequestType(fallbackType);
        }
    }

    private void updateRequestWithNewCard(CardRequest request, Card newCard, CardRequestType requestType) {
        if (newCard != null && newCard.getId() != null) {
            request.setCardId(newCard.getId());
        }
        request.setCard(newCard);
        // Status should remain APPROVED - will be changed to FINISHED in processCardRequest
        request.setRequestType(requestType);
        if (request.getId() != null) {
            request.setLastRequestId(request.getId().getValue());
        }
    }

    private Long requireCardId(Card card) throws CardDomainException {
        if (card == null || card.getId() == null) {
            throw logAndBuildException(
                    "Card id is missing for maintenance operation",
                    "Card id is required for card maintenance operations",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }
        return card.getId().getValue();
    }

    public Boolean existByTypeAndStatusAndPan(CardRequestType type, String pan) throws CardDomainException {
        log.info("check for duplicate request for card number : {}",PanMaskingUtil.maskPan(pan));
        List<Integer> statusList = new ArrayList<>();
        statusList.add(CardRequestStatus.INITIAL.getCode());
        statusList.add(CardRequestStatus.APPROVED.getCode());
        return repository.existByTypeAndStatusAndPan(type, pan, statusList);
    }

    private void existByTypeAndStatusAndCardId(CardRequestType type, Long cardId) throws CardDomainException {
        log.info("check for duplicate request for cardId : {}", cardId);
        List<Integer> statusList = new ArrayList<>();
        statusList.add(CardRequestStatus.INITIAL.getCode());
        statusList.add(CardRequestStatus.APPROVED.getCode());
        boolean exists = repository.existByTypeAndStatusAndCardId(type, cardId, statusList);
        if (exists) {
            throw logAndBuildException(
                    String.format("Duplicate request found for cardId: %s with type: %s", cardId, type),
                    String.format("A request already exists for this card with type: %s", type),
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public GenerateAccessFileForRequestResponse generateAccessFileForRequest(Long requestId) throws CardDomainException {
        log.info("Starting Access file generation for request ID: {}", requestId);

        // Get card request by ID
        CardRequest request = repository.findById(requestId)
                .orElseThrow(() -> logAndBuildException(
                        "CardRequest not found with id " + requestId,
                        "CardRequest not found: " + requestId,
                        StatusService.REQUEST_NOT_FOUND,
                        HttpStatus.NOT_FOUND));

        // Check if request status is PRINT_EXPECT
        if (!CardRequestStatus.ISSUED.equals(request.getRequestStatus())) {
            log.warn("CardRequest status is not ISSUED. Current status: {}, RequestId: {}",
                    request.getRequestStatus(), requestId);
            throw logAndBuildException(
                    String.format("CardRequest status must be PRINT_EXPECT. Current status: %s", request.getRequestStatus()),
                    String.format("درخواست باید در وضعیت 'صادر شده' باشد. وضعیت فعلی: %s",
                            request.getRequestStatus() != null ? request.getRequestStatus().getDescription() : "نامشخص"),
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }

        // Get card from request
        Card card = request.getCard();
        if (card == null) {
            log.warn("Card not found for request ID: {}", requestId);
            throw logAndBuildException(
                    "Card not found for this request",
                    "کارت متصل به این درخواست یافت نشد",
                    StatusService.CARD_NOT_FOUND,
                    HttpStatus.NOT_FOUND);
        }

        // Generate Access file for the card
        List<Card> cards = List.of(card);
        AccessFileResult result = cardPrintExporter.export(cards);

        // Generate secure hashId for file download
        String hashId = generateSecureHashId(requestId, result.getFileName());

        // Save file download record
        FileDownload fileDownload = FileDownload.builder()
                .hashId(hashId)
                .filePath(result.getFilePath() != null ? result.getFilePath().toString() : null)
                .fileName(result.getFileName())
                .requestId(requestId)
                .expiresAt(calculateExpirationTime())
                .isUsed(false)
                .build();

        fileDownloadRepository.save(fileDownload);

        log.info("Access file generated successfully for request ID: {}. File: {}, HashId: {}",
                requestId, result.getFileName(), hashId);

        return GenerateAccessFileForRequestResponse.builder()
                .requestId(requestId)
                .hashId(hashId)
                .message("فایل اکسس با موفقیت ایجاد شد")
                .build();
    }

    /**
     * Generate secure hashId for file download
     */
    private String generateSecureHashId(Long requestId, String fileName) {
        String data = requestId + "_" + fileName + "_" + System.currentTimeMillis();
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            log.error("Error generating hashId", e);
            // Fallback to UUID if SHA-256 is not available
            return java.util.UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * Generate secure hashId for batch file download
     */
    private String generateSecureHashIdForBatch(List<Long> requestIds, String fileName) {
        String requestIdsStr = requestIds.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
        String data = requestIdsStr + "_" + fileName + "_" + System.currentTimeMillis();
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            log.error("Error generating hashId for batch", e);
            // Fallback to UUID if SHA-256 is not available
            return java.util.UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * Convert bytes to hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    @Override
    public BatchProcessCardRequestsResponse batchProcessCardRequests(List<Long> requestIds) throws CardDomainException {
        log.info("Starting batch processing for {} request IDs", requestIds.size());

        List<Card> validCards = new ArrayList<>();
        List<Long> processedRequestIds = new ArrayList<>();
        List<Long> invalidRequestIds = new ArrayList<>();

        // Validate each request and collect valid cards
        for (Long requestId : requestIds) {
            try { log.info("find request with id: {} for creating access file", requestId);
                CardRequest request = repository.findById(requestId)
                        .orElseThrow(() -> {
                            log.warn("CardRequest not found - RequestId: {}", requestId);
                            return new CardDomainException(
                                    "CardRequest not found: " + requestId,
                                    StatusService.REQUEST_NOT_FOUND,
                                    HttpStatus.NOT_FOUND);
                        });

                // Check if request status is ISSUED
                if (!CardRequestStatus.ISSUED.equals(request.getRequestStatus()) ||
                         CardIssueMethod.VIRTUAL.equals(request.getCardProfile().getCardIssueMethod())) {
                    log.warn("CardRequest status is not ISSUED - RequestId: {}, Status: {}",
                            requestId, request.getRequestStatus());
                    invalidRequestIds.add(requestId);
                    continue;
                }

                // Check if card exists
                Card card = request.getCard();
                if (card == null) {
                    log.warn("Card not found for request - RequestId: {}", requestId);
                    invalidRequestIds.add(requestId);
                    continue;
                }

                // Valid request with card
                validCards.add(card);
                processedRequestIds.add(requestId);

            } catch (CardDomainException e) {
                log.warn("Error processing request ID {}: {}", requestId, e.getMessage());
                invalidRequestIds.add(requestId);
            }
        }

        // Check if we have any valid requests
        if (validCards.isEmpty()) {
            log.warn("No valid requests found for batch processing");
            throw logAndBuildException(
                    "No valid requests found. All requests must have ISSUED status and associated card.",
                    "هیچ درخواست معتبری یافت نشد. تمام درخواست‌ها باید دارای وضعیت ISSUED و کارت مرتبط باشند.",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }

        // Generate Access file for valid cards
        AccessFileResult accessResult = cardPrintExporter.export(validCards);

        // Generate secure hashId for file download
        String hashId = generateSecureHashIdForBatch(processedRequestIds, accessResult.getFileName());

        // Save file download record
        FileDownload fileDownload = FileDownload.builder()
                .hashId(hashId)
                .filePath(accessResult.getFilePath() != null ? accessResult.getFilePath().toString() : null)
                .fileName(accessResult.getFileName())
                .requestId(processedRequestIds.isEmpty() ? null : processedRequestIds.get(0)) // First request ID as reference
                .expiresAt(calculateExpirationTime())
                .isUsed(false)
                .build();

        fileDownloadRepository.save(fileDownload);

        // Update status of valid requests to PRINT_EXPECT
        for (Long requestId : processedRequestIds) {
            try {
                CardRequest request = repository.findById(requestId)
                        .orElseThrow(() -> {
                            log.error("CardRequest not found during status update - RequestId: {}", requestId);
                            return new CardDomainException(
                                    "CardRequest not found: " + requestId,
                                    StatusService.REQUEST_NOT_FOUND,
                                    HttpStatus.NOT_FOUND);
                        });

                request.setRequestStatus(CardRequestStatus.PRINT_EXPECT);
                repository.save(request);
                log.debug("Updated request status to PRINT_EXPECT - RequestId: {}", requestId);

            } catch (Exception e) {
                log.error("Error updating request status - RequestId: {}, Error: {}", requestId, e.getMessage(), e);
                throw new CardDomainException("Generate file error",
                        StatusService.GENERATE_ACCESS_FILE_ERROR, HttpStatus.OK);
                // Continue with other requests even if one fails
            }
        }

        log.info("Batch processing completed - Valid: {}, Invalid: {}, File: {}, HashId: {}",
                processedRequestIds.size(), invalidRequestIds.size(), accessResult.getFileName(), hashId);

        return BatchProcessCardRequestsResponse.builder()
                .hashId(hashId)
                .totalRequestIds(requestIds.size())
                .validRequestsCount(processedRequestIds.size())
                .invalidRequestsCount(invalidRequestIds.size())
                .processedRequestIds(processedRequestIds)
                .invalidRequestIds(invalidRequestIds)
                .message(String.format("پردازش دسته‌ای با موفقیت انجام شد. %d درخواست معتبر پردازش شد.",
                        processedRequestIds.size()))
                .build();
    }

    /**
     * Calculate expiration time (24 hours from now)
     */
    private java.sql.Timestamp calculateExpirationTime() {
        java.time.LocalDateTime expiration = java.time.LocalDateTime.now().plusHours(24);
        return java.sql.Timestamp.valueOf(expiration);
    }

    private CardDomainException logAndBuildException(String logMessage,
                                                     String userMessage,
                                                     int statusCode,
                                                     HttpStatus httpStatus) {
        log.info(logMessage);
        return new CardDomainException(userMessage, statusCode, httpStatus);
    }

}
