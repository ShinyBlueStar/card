package com.sample.system.card.service.domain.response.number;

public record GetCardNumberPatternResponse(
        Long id,
        String cardNumberFrom,
        String cardNumberTo,
        Long cardProfileId,
        String productCode,
        String name,
        String cardNumGenerationMethod,
        String patternStatus,
        String firstCardNumber,
        String lastCardNumber,
        String nextCardNumber,
        String createdBy,
        String createdDate,
        String lastModifiedBy,
        String lastModifiedDate,
        String message) {
}
