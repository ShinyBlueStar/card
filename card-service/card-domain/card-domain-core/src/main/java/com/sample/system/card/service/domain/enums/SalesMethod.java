package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SalesMethod implements CodedEnum {

    BRANCH(1, "فروش در شعبه"),
    UNLIMITED(2, "بدون محدودیت"),
    DEFAULT(3, "پیش فرض");

    private final Integer code;
    private final String description;

    public static SalesMethod fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SalesMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new InvalidInputParameterException("salesMethod", code.toString(), "Invalid sales method code");
    }
}
