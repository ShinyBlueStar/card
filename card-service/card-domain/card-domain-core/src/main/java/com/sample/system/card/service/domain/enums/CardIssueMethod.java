package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardIssueMethod implements CodedEnum {//نحوه صدور کارت

    PHYSICAL(0, "چاپ کارت"),
    VIRTUAL(1, "مجازی");

    private final Integer code;
    private final String description;

    public static CardIssueMethod fromCode(Integer code) {
        if (code == null) {
            throw new InvalidInputParameterException("CardIssueMethod", "null", "Code cannot be null");
        }
        for (CardIssueMethod status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new InvalidInputParameterException("CardIssueMethod", code.toString(), "Invalid Card Issue Method code");
    }

    public static Boolean convertToBoolean(CardIssueMethod cardIssueMethod) {
        if (cardIssueMethod == null) {
            throw new InvalidInputParameterException("CardIssueMethod", "null", "Card Issue Method cannot be null");
        }

        return switch (cardIssueMethod) {
            case PHYSICAL -> false;
            case VIRTUAL  -> true;
        };
    }
}
