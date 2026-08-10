package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardRequestType implements CodedEnum {

    CARD_ISSUANCE(1, "صدور کارت"),
    CARD_PRINT(2, "چاپ اولین صدور کارت"),
    CARD_RENEWAL(3, "تمدید کارت"),
    CHANGE_PIN(4, "تغییر رمز"),
    CARD_REISSUANCE(5, "درخواست صدور مجدد کارت"),
    CARD_REPLACEMENT(6, "درخواست کارت المثنی"),
    CARD_CORRECTION(7, "درخواست اصلاح کارت");

    private final Integer code;
    private final String description;

    public static CardRequestType fromCode(Integer code) {
        if (code == null) {
            throw new InvalidInputParameterException("CardRequestType", "null", "Code cannot be null");
        }
        for (CardRequestType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new InvalidInputParameterException("CardRequestType", code.toString(), "Invalid Card Request Type code");
    }
}
