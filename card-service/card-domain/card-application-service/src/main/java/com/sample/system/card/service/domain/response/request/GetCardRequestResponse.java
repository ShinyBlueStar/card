package com.sample.system.card.service.domain.response.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sample.system.card.service.domain.response.base.EnumResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetCardRequestResponse  {
    private final Long cardRequestId;
    private final String nationalId;
    private final String requestStatus;
    private final EnumResponse requestStatusEnum;
    private final List<EnumResponse> nextStatuses;
    private final String cardNumber;
    private final Long cardId;
    private final LocalDateTime requestDateFrom;
    private final LocalDateTime requestDateTo;
    private final EnumResponse requestType;
    private final Long reasonId;
    private final Long cardProfileId;
    private final Long profileId;
    private final String profileName;
    private final String caseNumber;
    private final String unitCode;
    private final String unitName;
    private final String firstName;
    private final String lastName;
    private final String createdDate;
    private final String createdBy;
    private final String lastModifiedDate;
    private final String lastModifiedBy;
    private final String issuerPersonId;
    private final String message;
}
