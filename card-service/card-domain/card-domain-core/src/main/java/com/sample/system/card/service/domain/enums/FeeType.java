package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeeType implements CodedEnum {
    EVENT_BASED(1, "رخ دادی"),
    PERIOD_BASED(2, "دوره ای"),
    TRANSACTION_BASED(3, "تراکنشی");

    private final Integer code;
    private final String description;

    public static FeeType fromCode(Integer code) {
        if (code == null) {
            throw new InvalidInputParameterException("feeType", "null", "Code cannot be null");
        }
        for (FeeType method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new InvalidInputParameterException("feeType", code.toString(),
                "Invalid fee type code");
    }
}
