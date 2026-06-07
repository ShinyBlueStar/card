package com.sample.system.card.service.domain.response.base;

import com.sample.system.card.service.domain.enums.CodedEnum;

/**
 * Generic response object for enum values
 * Contains code and description for enum display
 */
public record EnumResponse(
        Integer code,
        String description
) {
    public static EnumResponse fromEnum(CodedEnum enumValue) {
        return enumValue == null ? null : new EnumResponse(enumValue.getCode(), enumValue.getDescription());
    }
}
