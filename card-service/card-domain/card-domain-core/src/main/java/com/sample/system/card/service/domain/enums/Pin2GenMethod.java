package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Pin2GenMethod implements CodedEnum {
    CONSTANT(1, "ثابت"),
    DYNAMIC(2, "پویا"),
    COMPOSITE(3, "ترکیبی");

    private final Integer code;
    private final String description;

    public static Pin2GenMethod fromCode(Integer code) {
        if (code == null) {
            throw new InvalidInputParameterException("Pin2GenMethod", "null", "Code cannot be null");
        }
        for (Pin2GenMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new InvalidInputParameterException("Pin2GenMethod", code.toString(),
                "Invalid fee type code");
    }
}
