package com.sample.system.card.service.domain.utility.date;

import com.sample.system.card.service.domain.utility.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Slf4j
public final class DateUtils {

    public static final String PERSIAN_DATE_FORMAT = "yyyy/MM/dd";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final Locale FARSI_LOCALE = Locale.of("fa");
    public static final Locale ENGLISH_LOCALE = Locale.of("en");

    private DateUtils() {
    }

    /**
     * Parses a date with the given pattern (Jalali when the locale is Farsi).
     * If {@code strict} is true, the value must have exactly the pattern's length,
     * so '2/12/1999' does not match 'MM/dd/yyyy'.
     *
     * @return the parsed date, or {@code null} if the value is missing or invalid
     */
    public static Date parse(String value, String datePattern, boolean strict, Locale locale) {
        if (value == null || datePattern == null || datePattern.isEmpty()) {
            return null;
        }
        DateFormat formatter = isFarsi(locale)
                ? new PersianDateFormat(datePattern)
                : new SimpleDateFormat(datePattern);
        formatter.setLenient(false);
        try {
            Date date = formatter.parse(value);
            return strict && datePattern.length() != value.length() ? null : date;
        } catch (ParseException ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    /**
     * Formats a date (Jalali when the locale is Farsi). With {@code translate},
     * digits are converted to Persian digits.
     */
    public static String getLocaleDate(Locale locale, Date date, String format, boolean translate) {
        if (date == null) {
            return null;
        }
        boolean farsi = isFarsi(locale);
        DateFormat dateFormat = farsi
                ? new PersianDateFormat(format == null ? PERSIAN_DATE_FORMAT : format)
                : new SimpleDateFormat(format == null ? DEFAULT_DATE_FORMAT : format, Locale.getDefault());

        String result = translate
                ? StringUtils.getLocaleText(locale, dateFormat.format(date))
                : dateFormat.format(date);

        if (farsi && format != null && !format.startsWith("yyyy") && format.startsWith("yy")) {
            result = result.substring(2);
        }
        return result;
    }

    private static boolean isFarsi(Locale locale) {
        return locale != null && FARSI_LOCALE.getLanguage().equals(locale.getLanguage());
    }
}
