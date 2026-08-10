package com.sample.system.card.service.domain.handler.Command;

import com.sample.system.card.service.domain.command.GenerateAccessFileForRequestCommand;
import com.sample.system.card.service.domain.command.cardRequest.*;
import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.enums.CardRequestStatus;
import com.sample.system.card.service.domain.event.cardRequest.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardRequestDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardRequestService;
import com.sample.system.card.service.domain.response.request.CreateCardRequestResponse;
import com.sample.system.card.service.domain.response.request.UpdateCardRequestResponse;
import com.sample.system.card.service.domain.response.request.GenerateAccessFileForRequestResponse;
import com.sample.system.card.service.domain.response.request.BatchProcessCardRequestsResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@AllArgsConstructor
public class CardRequestCommandHandler {

    private final CardRequestService cardRequestService;
    private final CardRequestDataMapper cardRequestDataMapper;

    @Transactional
    public CreateCardRequestResponse createCardRequest(CreateIssueRequestCommand command) throws CardDomainException {
        log.info("Starting ISSUE CardRequest service - NationalId: {}, CardProfileId: {}",
                command.getNationalId(), command.getCardProfileId());

        CardRequest request = cardRequestDataMapper.createCommandToRequest(command);
        CardRequestCreatedEvent event = cardRequestService.createIssueCardRequest(request);
        log.debug("Mapping CardRequest to CreateCardRequestResponse");
        return cardRequestDataMapper.requestToCreateResponse(event.getCardRequest(), "success");
    }

    @Transactional
    public UpdateCardRequestResponse updateCardRequestStatus(UpdateCardRequestStatusCommand command) throws CardDomainException {
        log.info("Starting CardRequest status update service - RequestId: {} to status: {}",
                command.getCardRequestId(), CardRequestStatus.fromCode(command.getNewStatus()));

        CardRequest request = cardRequestDataMapper.updateCommandToRequest(command);
        CardRequestUpdatedEvent event = cardRequestService.updateCardRequestStatus(request);
        log.debug("CardRequest status update completed by CommandHandler - RequestId: {}",
                event.getCardRequest().getId().getValue());

        log.debug("Mapping CardRequest to UpdateCardRequestResponse");
        UpdateCardRequestResponse response = cardRequestDataMapper.requestToUpdateResponse(
                event.getCardRequest(), "Card Request status updated successfully!");

        log.info("CardRequest status update service completed successfully - RequestId: {}, NationalId: {}",
                event.getCardRequest().getId().getValue(), event.getCardRequest().getNationalId());
        return response;
    }

    @Transactional
    public UpdateCardRequestResponse processCardRequest(ProcessCardRequestCommand command) throws CardDomainException {
        log.info("Starting CardRequest processing service - RequestId: {}, RequestType: {}",
                command.getCardRequestId(), command.getRequestType());

        log.debug("Mapping ProcessCardRequestCommand to CardRequest domain entity");
        CardRequest request = cardRequestDataMapper.processCommandToRequest(command);
        log.debug("Command mapping completed successfully");
        CardRequestProcessedEvent event = cardRequestService.processCardRequest(request);
        log.debug("CardRequest processing completed by CommandHandler - RequestId: {}",
                event.getCardRequest().getId().getValue());

        log.debug("Mapping CardRequest to UpdateCardRequestResponse");
        UpdateCardRequestResponse response = cardRequestDataMapper.requestToUpdateResponse(
                event.getCardRequest(), "Card Request processed successfully!");

        log.info("CardRequest processing service completed successfully - RequestId: {}, NationalId: {}",
                event.getCardRequest().getId().getValue(), event.getCardRequest().getNationalId());
        return response;
    }

    @Transactional
    public UpdateCardRequestResponse cancelCardRequest(CancelCardRequestCommand command) throws CardDomainException {
        log.info("Starting CardRequest cancellation service - RequestId: {}", command.getCardRequestId());

        CardRequestCancelledEvent event = cardRequestService.cancelCardRequest(command.getCardRequestId());
        log.debug("CardRequest cancellation completed by CommandHandler - RequestId: {}",
                event.getCardRequest().getId().getValue());

        log.debug("Mapping CardRequest to UpdateCardRequestResponse");
        UpdateCardRequestResponse response = cardRequestDataMapper.requestToUpdateResponse(
                event.getCardRequest(), "Card Request cancelled successfully!");

        log.info("CardRequest cancellation service completed successfully - RequestId: {}, NationalId: {}",
                event.getCardRequest().getId().getValue(), event.getCardRequest().getNationalId());
        return response;
    }

    @Transactional
    public CreateCardRequestResponse createReplaceCardRequest(CreateReplacementRequestCommand command) throws CardDomainException {//المثنی
        log.info("Creating replaceCard request for CardId: {}", command.getCardId());
        CardRequest request = cardRequestDataMapper.createReplacementToRequestCommand(command);
        CardRequestCreatedEvent event = cardRequestService.createReplaceCardRequest(request);
        CreateCardRequestResponse response = cardRequestDataMapper.requestToCreateResponse(
                event.getCardRequest(), "Card replaced successfully!");
        log.info("Card replacement completed - CardRequestId: {}", response.getCardRequestId());
        return response;
    }

    @Transactional
    public CreateCardRequestResponse createRenewCardRequest(CreateRenewRequestCommand command) throws CardDomainException {//تمدید کارت
        log.info("Creating renewCard request for CardId: {}", command.getCardId());
        CardRequest request = cardRequestDataMapper.createRenewToRequestCommand(command);
        CardRequestCreatedEvent event = cardRequestService.createRenewCardRequest(request);
        CreateCardRequestResponse response = cardRequestDataMapper.requestToCreateResponse(
                event.getCardRequest(), "Card renewal request created successfully!");
        log.info("Card renewal request completed - CardRequestId: {}", response.getCardRequestId());
        return response;
    }

    @Transactional
    public GenerateAccessFileForRequestResponse generateAccessFileForRequest(GenerateAccessFileForRequestCommand command) throws CardDomainException {
        log.info("Starting Access file generation for request ID: {}", command.getRequestId());
        GenerateAccessFileForRequestResponse response = cardRequestService.generateAccessFileForRequest(command.getRequestId());
        log.info("Access file generation completed successfully - RequestId: {}, HashId: {}",
                command.getRequestId(), response.getHashId());
        return response;
    }

    @Transactional
    public BatchProcessCardRequestsResponse batchProcessCardRequests(BatchProcessCardRequestsCommand command) throws CardDomainException {
        log.info("Starting batch processing for {} request IDs", command.getRequestIds().size());
        BatchProcessCardRequestsResponse response = cardRequestService.batchProcessCardRequests(command.getRequestIds());
        log.info("Batch processing completed - Valid: {}, Invalid: {}, File: {}",
                response.getValidRequestsCount(), response.getInvalidRequestsCount(), response.getHashId());
        return response;
    }
}