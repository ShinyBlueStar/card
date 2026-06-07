package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActionType implements CodedEnum {

    CARD_ISSUANCE(1, "صدور کارت"),
    CARD_PRINT(2, "چاپ اولین صدور کارت"),
    CARD_RENEWAL(3, "تمدید کارت"),
    CHANGE_PIN(4, "تغییر رمز"),
    NEW_CARD(5, "درخواست کارت جدید"),
    CARD_REPLACEMENT(6, "درخواست کارت المثنی"),
    CARD_CORRECTION(7, "درخواست اصلاح کارت"),
    CARD_BLOCK(8, "مسدودسازی کارت"),
    CARD_UNBLOCK(9, "غیرمسدودسازی کارت"),
    CARD_ACTIVATE(10, "فعال‌سازی کارت"),
    CARD_DEACTIVATE(11, "غیرفعال‌سازی کارت");

    private final Integer code;
    private final String description;

    public static ActionType fromCode(Integer code) {
        if (code == null) {
            throw new InvalidInputParameterException("ACTION Type", "null", "Code cannot be null");
        }
        for (ActionType method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new InvalidInputParameterException("ACTION TYPE", code.toString(),
                "Invalid fee type code");
    }
}
