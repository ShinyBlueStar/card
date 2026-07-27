package com.sample.system.card.service.domain.utility;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PanMaskingUtilTest {

    @Test
    void masksMiddleOfSixteenDigitPan() {
        assertThat(PanMaskingUtil.maskPan("6037991234567890")).isEqualTo("603799******7890");
    }

    @Test
    void trimsBeforeMasking() {
        assertThat(PanMaskingUtil.maskPan("  6037991234567890 ")).isEqualTo("603799******7890");
    }

    @ParameterizedTest
    @CsvSource({
            "1234567890, 1********0",
            "123, 1*3",
            "12, **",
            "1, *"
    })
    void masksShortValuesExceptFirstAndLastCharacter(String input, String expected) {
        assertThat(PanMaskingUtil.maskPan(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void returnsNullForMissingPan(String input) {
        assertThat(PanMaskingUtil.maskPan(input)).isNull();
    }

    @Test
    void neverExposesMoreThanTenDigits() {
        String masked = PanMaskingUtil.maskPan("6219861012345678901");
        assertThat(masked.replace("*", "")).hasSize(10);
    }
}
