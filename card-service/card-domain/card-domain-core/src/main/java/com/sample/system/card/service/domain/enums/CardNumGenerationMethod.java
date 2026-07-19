package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardNumGenerationMethod implements CodedEnum {

    INCREMENTAL(1, "افزایشی"),
    RANGE_BASED(2, "با استفاده از بازه شماره کارت");

    private final Integer code;
    private final String description;

    /**
     * Find CardNumGenerationMethod by code
     * @param code the code to search for
     * @return CardNumGenerationMethod enum
     * @throws InvalidInputParameterException if code is not found
     */
    public static CardNumGenerationMethod fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CardNumGenerationMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new InvalidInputParameterException("cardNumGenerationMethod", code.toString(), "Invalid card number generation method code");
    }
}
