package com.sample.system.card.service.domain.utility;

import java.util.Arrays;
import java.util.Locale;

public final class StringUtils {

    private static final String FARSI_LANGUAGE = "fa";
    private static final int PERSIAN_DIGIT_OFFSET = 0x6C0;

    private StringUtils() {
    }

    /**
     * For the Farsi locale, converts Latin digits in the text to Persian digits.
     */
    public static String getLocaleText(Locale locale, String text) {
        if (text == null || locale == null || !FARSI_LANGUAGE.equalsIgnoreCase(locale.getLanguage())) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length());
        text.chars()
                .map(c -> c >= '0' && c <= '9' ? c + PERSIAN_DIGIT_OFFSET : c)
                .forEach(c -> sb.append((char) c));
        return sb.toString();
    }

    public static String fixSomeWord(String statement) {
        if (Arrays.asList(statement.split(" ")).contains("نسده")) {
            return statement.replace("نسده", "نشده");
        }
        return statement;
    }

    public static boolean isPersianString(String str) {
        return str != null && str.chars().anyMatch(c -> c > 127);
    }
}
