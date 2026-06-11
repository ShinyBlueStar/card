package com.sample.system.card.service.domain.response.bank;

public record BankResponse(
        Long id,
        String name,
        String binCode,
        Boolean isActive,
        String description) {
}
