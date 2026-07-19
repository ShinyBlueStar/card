package com.sample.system.card.service.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberPatternTest {

    private static CardNumberPattern range(String from, String to) {
        return CardNumberPattern.builder().cardNumberFrom(from).cardNumberTo(to).build();
    }

    @ParameterizedTest
    @CsvSource({
            "1000000, true",
            "1500000, true",
            "1999999, true",
            "0999999, false",
            "2000000, false"
    })
    void containsChecksInclusiveBounds(String cardNumber, boolean expected) {
        assertThat(range("1000000", "1999999").contains(cardNumber)).isEqualTo(expected);
    }

    @Test
    void containsIsFalseWhenAnyValueIsMissing() {
        assertThat(range(null, "1999999").contains("1500000")).isFalse();
        assertThat(range("1000000", "1999999").contains(null)).isFalse();
    }

    @Test
    void validRangeNeedsSevenDigitBoundsInOrder() {
        assertThat(range("1000000", "1999999").isValidRange()).isTrue();
        assertThat(range("1999999", "1000000").isValidRange()).isFalse();
        assertThat(range("100000", "1999999").isValidRange()).isFalse();
        assertThat(range("10000AB", "1999999").isValidRange()).isFalse();
        assertThat(range(null, "1999999").isValidRange()).isFalse();
    }

    @Test
    void exhaustedWhenNextNumberPassesTheEnd() {
        CardNumberPattern pattern = range("1000000", "1000010").toBuilder().nextCardNumber("1000011").build();
        CardNumberPattern notYet = pattern.toBuilder().nextCardNumber("1000010").build();

        assertThat(pattern.isExhausted()).isTrue();
        assertThat(notYet.isExhausted()).isFalse();
    }
}
