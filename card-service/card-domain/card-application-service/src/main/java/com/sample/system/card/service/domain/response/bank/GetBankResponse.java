package com.sample.system.card.service.domain.response.bank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetBankResponse {
    private final Long id;
    private final String binCode;
    private final String name;
    private final String description;
    private final Boolean isActive;
}