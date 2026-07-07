package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.valueObject.CardNumberPatternId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardProfileTest {

    @Test
    void validityPeriodMustBePositive() throws CardDomainException {
        CardProfile profile = new CardProfile();

        assertThatThrownBy(() -> profile.setValidityPeriod(0)).isInstanceOf(CardDomainException.class);

        profile.setValidityPeriod(36);
        assertThat(profile.getValidityPeriod()).isEqualTo(36);
    }

    @Test
    void categoryAndTypeMustBeSentTogether() {
        assertThatCode(() -> CardProfile.validateCardProfileCreation(null, null)).doesNotThrowAnyException();
        assertThatCode(() -> CardProfile.validateCardProfileCreation(1L, 2L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> CardProfile.validateCardProfileCreation(null, 2L))
                .isInstanceOf(CardDomainException.class);
        assertThatThrownBy(() -> CardProfile.validateCardProfileCreation(1L, null))
                .isInstanceOf(CardDomainException.class);
    }

    @Test
    void activateAndDeactivate() {
        CardProfile profile = new CardProfile();

        profile.activate();
        assertThat(profile.isActive()).isTrue();

        profile.deactivate();
        assertThat(profile.isActive()).isFalse();
    }

    @Test
    void appendPatternIgnoresDuplicates() {
        CardProfile profile = new CardProfile();
        CardNumberPattern pattern = patternWithId(7L);

        profile.appendPattern(pattern);
        profile.appendPattern(patternWithId(7L));

        assertThat(profile.getCardNumberRangeList()).containsExactly(pattern);
        assertThat(profile.getNumPatternId()).isEqualTo(7L);
    }

    @Test
    void replacePatternKeepsOnlyTheNewOne() {
        CardProfile profile = new CardProfile();
        profile.appendPattern(patternWithId(1L));
        CardNumberPattern replacement = patternWithId(2L);

        profile.replacePattern(replacement);

        assertThat(profile.getCardNumberRangeList()).containsExactly(replacement);
        assertThat(profile.getNumPattern()).isSameAs(replacement);
    }

    private static CardNumberPattern patternWithId(long id) {
        CardNumberPattern pattern = CardNumberPattern.builder().name("P" + id).build();
        pattern.setId(new CardNumberPatternId(id));
        return pattern;
    }
}
