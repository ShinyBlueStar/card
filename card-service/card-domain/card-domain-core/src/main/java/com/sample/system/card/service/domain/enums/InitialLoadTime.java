package com.sample.system.card.service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InitialLoadTime implements CodedEnum {

    ISSUE_TIME(1, "زمان صدور"),
    DELIVERY_TIME(2, "زمان تحویل");

    private final Integer code;
    private final String description;
}
