package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Fee Period enumeration
 * Maps to Integer codes in database
 */
@Getter
@RequiredArgsConstructor
public enum FeePeriod implements CodedEnum {

    DAILY(1, "روزانه"),
    WEEKLY(2, "هفتگی"),
    MONTHLY(3, "ماهانه"),
    YEARLY(4, "سالانه"),
    ONE_TIME(5, "یکباره");

    private final Integer code;
    private final String description;

    /**
     * Find FeePeriod by code
     * @param code the code to search for
     * @return FeePeriod enum
     * @throws InvalidInputParameterException if code is not found
     */
    public static FeePeriod fromCode(Integer code) {
        if (code == null) {
            throw new InvalidInputParameterException("feePeriod", "null", "Code cannot be null");
        }
        for (FeePeriod period : values()) {
            if (period.code.equals(code)) {
                return period;
            }
        }
        throw new InvalidInputParameterException("feePeriod", code.toString(), "Invalid fee period code");
    }
}
