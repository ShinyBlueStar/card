package com.sample.system.card.service.domain.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Codes are persisted in the database, so every coded enum must have unique, non-null codes.
 */
class CodedEnumTest {

    @ParameterizedTest
    @ValueSource(classes = {
            ActionRequestNextStatus.class, ActionType.class, CardChangeReason.class, CardIssueMethod.class,
            CardNumGenerationMethod.class, CardNumberPatternStatus.class, CardPhysicalStatus.class,
            CardProfileStatus.class, CardRenewalType.class, CardRequestStatus.class, CardRequestType.class,
            CardStatus.class, FeePeriod.class, FeeType.class, GeneralStatusEnum.class, InitialLoadTime.class,
            Pin2GenMethod.class, ReasonGroup.class, Rechargeable.class, Reloadable.class, RequestReason.class,
            SalesMethod.class
    })
    void codesAreUniqueAndDescribed(Class<? extends CodedEnum> type) {
        CodedEnum[] values = type.getEnumConstants();

        assertThat(values).allSatisfy(value -> {
            assertThat(value.getCode()).isNotNull();
            assertThat(value.getDescription()).isNotBlank();
        });
        assertThat(Arrays.stream(values).map(CodedEnum::getCode)).doesNotHaveDuplicates();
    }
}
