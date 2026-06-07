package com.sample.system.card.service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GeneralStatusEnum implements CodedEnum {
    ACTIVE(1, "فعال"),
    INACTIVE(2, "غیرفعال");

    private final Integer code;
    private final String description;

    public static GeneralStatusEnum fromCode(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("کد وضعیت الگو نمی‌تواند null باشد");
        }
        for (GeneralStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("کد وضعیت الگو نامعتبر است. فقط مقادیر 1 (فعال) و 2 (غیرفعال) مجاز هستند: " + code);
    }
}

