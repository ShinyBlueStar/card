package com.sample.system.card.service.domain.response.card;

public record GetCardResponse(
        Long cardId,
        String pan,
        String issueDate,
        String expirationDate,
        String cardHolderName,
        String cardHolderFamily,
        String address,
        String cardStatus,
        String cardType,
        String issuingBank,
        String unitName,
        String unitCode,
        String nationalId,
        String caseNumber,
        Boolean virtualCard,
        String createdBy,
        String createdDate,
        String lastModifiedBy,
        String lastModifiedDate) {
}
