package com.sample.system.card.service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestReason implements CodedEnum {
    NEW_REQUEST(1, "درخواست کارت جدید"),
    EXPIRED(2, "منقضی شدن کارت"),
    LOST(3, "مفقود شدن کارت"),
    STOLEN(4, "سرقت کارت"),
    DAMAGED(5, "تعویض کارت (آسیب‌دیده)");

    private final Integer code;
    private final String description;
}
