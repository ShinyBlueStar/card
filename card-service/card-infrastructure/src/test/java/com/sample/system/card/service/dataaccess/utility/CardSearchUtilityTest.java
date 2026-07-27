package com.sample.system.card.service.dataaccess.utility;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CardSearchUtilityTest {

    @ParameterizedTest
    @ValueSource(strings = {"true", "TRUE", " 1 ", "yes", "Y"})
    void parsesTruthyValues(String value) {
        assertThat(CardSearchUtility.parseBooleanValue(value)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "0", "No", "n"})
    void parsesFalsyValues(String value) {
        assertThat(CardSearchUtility.parseBooleanValue(value)).isFalse();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "maybe", "2"})
    void returnsNullForAnythingElse(String value) {
        assertThat(CardSearchUtility.parseBooleanValue(value)).isNull();
    }
}
