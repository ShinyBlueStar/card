package com.sample.system.card.service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardPhysicalStatus implements CodedEnum {

    IN_PRINTING(1, "در حال چاپ"),
    PRINTED(2, "چاپ شده"),
    DELIVERED(3, "تحویل داده شده"),
    DAMAGED(4, "آسیب دیده"),
    LOST(5, "گم شده");

    /** کد عددی وضعیت فیزیکی */
    private final Integer code;

    /** توضیح فارسی وضعیت فیزیکی */
    private final String description;

    /**
     * دریافت مقدار Enum از روی کد عددی
     */
    public static CardPhysicalStatus fromCode(int code) {
        for (CardPhysicalStatus status : values()) {
            if (status.code == code)
                return status;
        }
        throw new IllegalArgumentException("کد وضعیت فیزیکی کارت نامعتبر است: " + code);
    }

}
