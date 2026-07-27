package com.sample.system.card.service.domain.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardStatusTest {

    @ParameterizedTest
    @EnumSource(CardStatus.class)
    void fromCodeRoundTrips(CardStatus status) {
        assertThat(CardStatus.fromCode(status.getCode())).isEqualTo(status);
    }

    @Test
    void fromCodeRejectsUnknownCode() {
        assertThatThrownBy(() -> CardStatus.fromCode(42)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void onlyActiveCardCanBeBlocked() {
        assertThat(CardStatus.ACTIVE.canBeBlocked()).isTrue();
        assertThat(CardStatus.HOT.canBeBlocked()).isFalse();
        assertThat(CardStatus.ISSUED.canBeBlocked()).isFalse();
    }

    @Test
    void expiredAndInactiveAreTerminal() {
        assertThat(CardStatus.EXPIRED.isTerminalState()).isTrue();
        assertThat(CardStatus.INACTIVE.isTerminalState()).isTrue();
        assertThat(CardStatus.ACTIVE.isTerminalState()).isFalse();
    }
}
