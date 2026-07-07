package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Reloadable implements CodedEnum {

    NON_RELOADABLE(1, "غیر قابل شارژ"),
    RELOADABLE(2, "قابل شارژ"),
    DEFAULT(3, "پیش فرض");

    private final Integer code;
    private final String description;

    public static Reloadable fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (Reloadable method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new InvalidInputParameterException("Reloadable", code.toString(), "Invalid Reloadable method code");
    }
}
