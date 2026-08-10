package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardRenewalType implements CodedEnum {

    RENEW_WITHOUT_PRINT(1, "تمدید بدون نیاز به چاپ"),
    RENEW_WITH_PRINT(2, "تمدید با الزام چاپ");

    private final Integer code;
    private final String description;

    /**
     * Find CardRenewalType by code
     * @param code the code to search for
     * @return CardRenewalType enum
     * @throws InvalidInputParameterException if code is not found
     */
    public static CardRenewalType fromCode(Integer code) {
        if (code == null) {
            throw new InvalidInputParameterException("cardRenewalType", "null", "Code cannot be null");
        }
        for (CardRenewalType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new InvalidInputParameterException("cardRenewalType", code.toString(), "Invalid card renewal type code");
    }
}
