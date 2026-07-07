package com.sample.system.card.service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Rechargeable implements CodedEnum {

    NON_RECHARGEABLE(0, "غیر قابل شارژ"),
    RECHARGEABLE(1, "قابل شارژ");

    private final Integer code;
    private final String description;
}
