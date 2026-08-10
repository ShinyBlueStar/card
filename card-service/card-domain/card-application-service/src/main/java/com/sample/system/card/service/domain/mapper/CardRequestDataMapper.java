package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.cardRequest.ProcessCardRequestCommand;
import com.sample.system.card.service.domain.command.cardRequest.CreateIssueRequestCommand;
import com.sample.system.card.service.domain.command.cardRequest.CreateReplacementRequestCommand;
import com.sample.system.card.service.domain.command.cardRequest.CreateRenewRequestCommand;
import com.sample.system.card.service.domain.command.cardRequest.GetCardRequestQuery;
import com.sample.system.card.service.domain.command.cardRequest.UpdateCardRequestStatusCommand;
import com.sample.system.card.service.domain.enums.ActionRequestNextStatus;
import com.sample.system.card.service.domain.enums.CardRequestStatus;
import com.sample.system.card.service.domain.enums.CardRequestType;
import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.response.base.EnumResponse;
import com.sample.system.card.service.domain.response.request.CreateCardRequestResponse;
import com.sample.system.card.service.domain.response.request.GetCardRequestResponse;
import com.sample.system.card.service.domain.response.request.UpdateCardRequestResponse;
import com.sample.system.card.service.domain.response.request.CardRequestListResponse;
import com.sample.system.card.service.domain.utility.date.DateUtils;
import com.sample.system.card.service.domain.valueObject.CardId;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import com.sample.system.card.service.domain.valueObject.CardRequestId;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Mapper for CardRequest domain entity and DTOs
 * Following DDD patterns for domain layer mapping
 */
@Component
public class CardRequestDataMapper {

    public CardRequest createCommandToRequest(CreateIssueRequestCommand createCommand) {
        CardProfileId profId = new CardProfileId(createCommand.getCardProfileId());
        return CardRequest.builder()
                .nationalId(createCommand.getNationalId())
                .issuerPersonId(createCommand.getIssuerPersonId())
                .caseNumber(createCommand.getCaseNumber())
                .unitCode(createCommand.getUnitCode())
                .unitName(createCommand.getUnitName())
                .firstName(createCommand.getFirstName())
                .lastName(createCommand.getLastName())
                .cardProfileId(profId)
                .profileId(createCommand.getCardProfileId())
                .build();
    }

    public CardRequest createReplacementToRequestCommand(CreateReplacementRequestCommand createCommand) {
        return CardRequest.builder()
                .nationalId(createCommand.getNationalId())
                .issuerPersonId(createCommand.getIssuerPersonId())
                .unitId(createCommand.getUnitId())
                .oldCardId(createCommand.getCardId())
                .reasonId(createCommand.getReasonId()).build();
    }

    public CardRequest createRenewToRequestCommand(CreateRenewRequestCommand createCommand) {
        CardRequest.CardRequestBuilder builder = CardRequest.builder()
                .nationalId(createCommand.getNationalId())
                .issuerPersonId(createCommand.getIssuerPersonId())
                .unitId(createCommand.getUnitId());

        if (createCommand.getCardId() != null) {
            builder.cardId(new CardId(createCommand.getCardId()));
        }

        return builder.build();
    }

    public CreateCardRequestResponse requestToCreateResponse(CardRequest request, String message) {
        return CreateCardRequestResponse.builder().cardRequestId(request.getId().getValue()).build();
    }

    public UpdateCardRequestResponse requestToUpdateResponse(CardRequest request, String message) {
        return new UpdateCardRequestResponse(request.getId().getValue(),message);
    }

    public GetCardRequestResponse requestToGetResponse(CardRequest request) {
        if (request == null) return null;

        String createdDatePersian = convertToPersianDate(request.getCreatedDate());
        String lastModifiedDatePersian = convertToPersianDate(request.getLastModifiedDate());

        // Calculate nextStatuses using resolveNextStatuses method
        List<ActionRequestNextStatus> nextStatuses = request.getRequestStatus() != null
                ? CardRequestStatus.resolveNextStatuses(request.getRequestStatus(), request.getCardProfile())
                : Collections.emptyList();

        // Convert nextStatuses to list of string descriptions
        List<EnumResponse> nextStatusDescriptions = nextStatuses.stream()
                .map(EnumResponse::fromEnum)
                .toList();

        return GetCardRequestResponse.builder()
                .cardRequestId(request.getId() != null ? request.getId().getValue() : null)
                .nationalId(request.getNationalId())
                .requestStatus(request.getRequestStatus() != null ? request.getRequestStatus().getDescription() : null)
                .requestStatusEnum(EnumResponse.fromEnum(request.getRequestStatus()))
                .nextStatuses(nextStatusDescriptions.isEmpty() ? null : nextStatusDescriptions)
                .requestType(EnumResponse.fromEnum(request.getRequestType()))
                .cardId(request.getCard()!= null && request.getCard().getId()!= null? request.getCard().getId().getValue():null)
                .cardNumber(request.getCard()!= null? request.getCard().getPan():null)
                .reasonId(request.getReasonId())
                .cardProfileId(request.getCardProfileId() != null ? request.getCardProfileId().getValue() : null)
                .profileId(request.getProfileId()) // Add profileId
                .profileName(request.getCardProfile() != null ? request.getCardProfile().getProfileName() : null) // Add profileName
                .cardId(request.getCardId() != null ? request.getCardId().getValue() : null)
                .caseNumber(request.getCaseNumber())
                .unitCode(request.getUnitCode())
                .unitName(request.getUnitName())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .createdDate(createdDatePersian)
                .createdBy(request.getCreatedBy())
                .lastModifiedDate(lastModifiedDatePersian)
                .lastModifiedBy(request.getLastModifiedBy())
                .issuerPersonId(request.getIssuerPersonId())
                .message("Card request retrieved successfully")
                .build();
    }

    private String convertToPersianDate(Timestamp date) {
        return Optional.ofNullable(date)
                .map(ts -> DateUtils.getLocaleDate(
                        DateUtils.FARSI_LOCALE,
                        ts,
                        DateUtils.PERSIAN_DATE_FORMAT,
                        false))
                .orElse(null);
    }

    public List<GetCardRequestResponse> requestsToGetResponses(List<CardRequest> requests) {
        return requests.stream()
                .map(this::requestToGetResponse)
                .toList();
    }

    public CardRequestListResponse requestsToGetListResponse(Page<CardRequest> cardRequests) {
        if (cardRequests == null) return null;
        CardRequestListResponse cardRequestListResponse = new CardRequestListResponse();
        cardRequestListResponse.setList(cardRequests.getContent().stream()
                .map(this::requestToGetResponse)
                .toList());
        cardRequestListResponse.setNumber(cardRequests.getNumber());
        cardRequestListResponse.setSize(cardRequests.getSize());
        cardRequestListResponse.setTotalElements(cardRequests.getTotalElements());
        cardRequestListResponse.setTotalPages(cardRequests.getTotalPages());
        return cardRequestListResponse;
    }

    public CardRequest queryToCardRequest(GetCardRequestQuery query) {
        if (query == null) {
            return null;
        }
        CardRequest request = CardRequest.builder().build();
        if (query.getCardRequestId() != null) {
            request.setId(new CardRequestId(query.getCardRequestId()));
        }
        return request;
    }

    public Card toCard(Long cardId) {
        Card card = Card.builder().build();
        if (cardId != null) {
            card.setId(new CardId(cardId));
        }
        return card;
    }

    public CardRequest updateCommandToRequest(UpdateCardRequestStatusCommand command) {
        if (command == null) {
            return null;
        }

        CardRequest request = CardRequest.builder()
                .requestStatus(command.getNewStatus() != null
                        ? CardRequestStatus.fromCode(command.getNewStatus())
                        : null)
                .build();

        if (command.getCardRequestId() != null) {
            request.setId(new CardRequestId(command.getCardRequestId()));
        }

        return request;
    }

    public CardRequest processCommandToRequest(ProcessCardRequestCommand command) {
        if (command == null) {
            return null;
        }

        CardRequest request = CardRequest.builder()
                .requestType(command.getRequestType() != null
                        ? CardRequestType.fromCode(command.getRequestType())
                        : null)
                .build();

        if (command.getCardRequestId() != null) {
            request.setId(new CardRequestId(command.getCardRequestId()));
        }

        return request;
    }

}
