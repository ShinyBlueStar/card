package com.sample.system.card.service.domain.response.cardType;

public record GetCardTypeResponse(
        Long id,
        String code,
        String name,
        String description,
        Boolean isActive) {
}