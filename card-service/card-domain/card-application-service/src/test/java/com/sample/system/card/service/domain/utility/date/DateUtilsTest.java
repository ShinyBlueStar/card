package com.sample.system.card.service.domain.utility.date;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilsTest {

    private static Date noon(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day).atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void formatsNowruzAsFirstDayOfJalaliYear() {
        String jalali = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, noon(2024, 3, 20),
                DateUtils.PERSIAN_DATE_FORMAT, false);

        assertThat(jalali).isEqualTo("1403/01/01");
    }

    @Test
    void formatsGregorianForNonFarsiLocale() {
        assertThat(DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, noon(2024, 3, 20), null, false))
                .isEqualTo("2024-03-20");
    }

    @Test
    void translateOptionUsesPersianDigits() {
        assertThat(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, noon(2024, 3, 20),
                DateUtils.PERSIAN_DATE_FORMAT, true)).isEqualTo("۱۴۰۳/۰۱/۰۱");
    }

    @Test
    void jalaliParseAndFormatRoundTrip() {
        Date parsed = DateUtils.parse("1403/06/31", DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);

        assertThat(parsed).isNotNull();
        assertThat(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, parsed, DateUtils.PERSIAN_DATE_FORMAT, false))
                .isEqualTo("1403/06/31");
    }

    @Test
    void parseReturnsNullForInvalidInput() {
        assertThat(DateUtils.parse(null, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE)).isNull();
        assertThat(DateUtils.parse("not-a-date", DateUtils.DEFAULT_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE)).isNull();
        assertThat(DateUtils.parse("2024-3-5", DateUtils.DEFAULT_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE))
                .as("strict mode requires the exact pattern length").isNull();
    }

    @Test
    void formattingNullDateReturnsNull() {
        assertThat(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, null, null, false)).isNull();
    }
}
