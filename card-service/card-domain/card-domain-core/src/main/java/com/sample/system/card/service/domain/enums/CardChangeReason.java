package com.sample.system.card.service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardChangeReason implements CodedEnum {
    NEW(1, "درخواست کارت جدید"),
    EXPIRED(2, "منقضی شدن کارت"),
    LOST(3, "مفقودی کارت"),
    STOLEN(4, "سرقت کارت"),
    DAMAGED(5, "تعویض به دلیل آسیب کارت");

    private final Integer code;
    private final String description;
}
