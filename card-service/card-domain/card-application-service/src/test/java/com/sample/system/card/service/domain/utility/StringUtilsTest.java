package com.sample.system.card.service.domain.utility;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @Test
    void convertsDigitsToPersianForFarsiLocale() {
        assertThat(StringUtils.getLocaleText(Locale.of("fa"), "1403/01/09")).isEqualTo("۱۴۰۳/۰۱/۰۹");
    }

    @Test
    void leavesTextUnchangedForOtherLocales() {
        assertThat(StringUtils.getLocaleText(Locale.ENGLISH, "1403/01/09")).isEqualTo("1403/01/09");
        assertThat(StringUtils.getLocaleText(null, "123")).isEqualTo("123");
        assertThat(StringUtils.getLocaleText(Locale.of("fa"), null)).isNull();
    }

    @Test
    void detectsNonAsciiText() {
        assertThat(StringUtils.isPersianString("کارت")).isTrue();
        assertThat(StringUtils.isPersianString("card")).isFalse();
        assertThat(StringUtils.isPersianString("")).isFalse();
        assertThat(StringUtils.isPersianString(null)).isFalse();
    }

    @Test
    void fixesCommonTypo() {
        assertThat(StringUtils.fixSomeWord("کارت یافت نسده است")).isEqualTo("کارت یافت نشده است");
        assertThat(StringUtils.fixSomeWord("کارت یافت نشد")).isEqualTo("کارت یافت نشد");
    }
}
