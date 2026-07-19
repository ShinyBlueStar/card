package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.enums.CardNumGenerationMethod;
import com.sample.system.card.service.domain.enums.CardNumberPatternStatus;
import com.sample.system.card.service.domain.valueObject.CardNumberPatternId;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@ToString
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CardNumberPattern extends BaseEntity<CardNumberPatternId> implements Serializable {
    private String cardNumberFrom;
    private String cardNumberTo;
    private CardProfile cardProfile;
    private CardProfileId cardProfileId;
    private CardNumberPatternStatus patternStatus;
    private String firstCardNumber;
    private String lastCardNumber;
    private String nextCardNumber;
    private CardNumGenerationMethod cardNumGenerationMethod;
    private String productCode;
    private String name;
    private String createdBy;
    private Timestamp createdDate;
    private String lastModifiedBy;
    private Timestamp lastModifiedDate;

    public boolean contains(String cardNumber) {
        if (cardNumberFrom == null || cardNumberTo == null || cardNumber == null) return false;
        return cardNumber.compareTo(cardNumberFrom) >= 0 && cardNumber.compareTo(cardNumberTo) <= 0;
    }

    public boolean isValidRange() {
        if (cardNumberFrom == null || cardNumberTo == null) {
            return false;
        }
        if (!isValidCardNumberFormat(cardNumberFrom) || !isValidCardNumberFormat(cardNumberTo)) {
            return false;
        }
        try {
            long start = Long.parseLong(cardNumberFrom);
            long end = Long.parseLong(cardNumberTo);
            return start <= end;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidCardNumberFormat(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) {
            return false;
        }

        String trimmed = cardNumber.trim();
        return trimmed.matches("\\d{7}");
    }

    public boolean isExhausted() {
        if (nextCardNumber == null || cardNumberTo == null) {
            return false;
        }
        try {
            long next = Long.parseLong(nextCardNumber);
            long end = Long.parseLong(cardNumberTo);
            return next > end;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}