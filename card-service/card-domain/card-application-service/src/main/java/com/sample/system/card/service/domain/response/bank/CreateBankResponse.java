package com.sample.system.card.service.domain.response.bank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Response for creating a new bank
 * Following CQRS pattern for write operations
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateBankResponse {

    private final Long bankId;
    private final String binCode;
    private final String name;
    private final Boolean isActive;
    private final String message;
}
