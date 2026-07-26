package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReasonGroup implements CodedEnum {
    BLOCK_CARD(1, "مسدودی کارت"),
    UNBLOCK_CARD(2, "غیرمسدود سازی کارت"),
    ACTIVE_CARD(3, "فعالسازی کارت"),
    INACTIVE_CARD(4, "غیرفعالسازی کارت");

    private final Integer code;
    private final String description;

    public static ReasonGroup fromCode(Integer code) {
        if (code == null) {
            throw new InvalidInputParameterException("ReasonGroup", "null", "Code cannot be null");
        }
        for (ReasonGroup type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new InvalidInputParameterException("ReasonGroup", code.toString(), "Invalid Group Reason code");
    }
}
