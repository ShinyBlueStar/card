package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * CardNumberPatternStatus Value Object
 * Represents the possible states of a card number pattern
 * Following DDD principles for value objects
 */
@Getter
@RequiredArgsConstructor
public enum CardNumberPatternStatus implements CodedEnum {

    ACTIVE(1, "فعال"),
    INACTIVE(2, "غیرفعال"),
    ASSIGN(3, "اختصاص یافته"),
    EXHAUSTED(4, "تمام شده");

    private final Integer code;
    private final String description;

    public static CardNumberPatternStatus fromCode(Integer code) {
        if (code == null) {
            throw new InvalidInputParameterException("patternStatus", "null", "Code cannot be null");
        }
        for (CardNumberPatternStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new InvalidInputParameterException("patternStatus", code.toString(), "Invalid pattern status code");
    }
}
