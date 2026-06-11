package com.sample.system.card.service.domain.response.bank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateBankResponse {
    private final Long bankId;
    private final String message;
    private final Boolean isActive;
}