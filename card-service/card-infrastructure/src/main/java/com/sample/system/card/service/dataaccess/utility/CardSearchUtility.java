package com.sample.system.card.service.dataaccess.utility;

import com.sample.system.card.service.dataaccess.card.entity.converter.EnumCodeConverter;
import com.sample.system.card.service.domain.enums.CodedEnum;
import com.sample.system.card.service.domain.utility.date.DateUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public final class CardSearchUtility {

    private CardSearchUtility() {}

    // فیلدهای اینامی انتیتی را با کد عددی جستجو میکند؛ کد نامعتبر هیچ نتیجه ای برنمیگرداند
    public static <E extends Enum<E> & CodedEnum> Predicate buildEnumCodePredicate(Root<?> root, CriteriaBuilder cb,
                                                                               String field, Class<E> type, Integer code) {
        if (code == null) return null;
        return EnumCodeConverter.find(type, code)
                .map(value -> cb.equal(root.get(field), value))
                .orElseGet(cb::disjunction);
    }

    public static Predicate buildExactMatchPredicate(Root<?> root, CriteriaBuilder cb, String field, String value) {
        if (StringUtils.isBlank(value)) return null;
        try {
            // Try numeric
            if (value.matches("^-?\\d+$")) {
                return cb.equal(root.get(field), Long.parseLong(value));
            }
        } catch (Exception ignored) {}
        return cb.equal(root.get(field).as(String.class), value);
    }

    public static Predicate buildFarsiLikePredicate(Root<?> root, CriteriaBuilder cb, String field, String value) {
        if (StringUtils.isBlank(value)) return null;
        String pattern = "%" + value.trim() + "%";
        return cb.like(root.get(field).as(String.class), pattern);
    }

    public static Boolean parseBooleanValue(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase();
        return switch (v) {
            case "true", "1", "yes", "y" -> true;
            case "false", "0", "no", "n" -> false;
            default -> null;
        };
    }

    public static Predicate buildNumericRangePredicate(Root<?> root, CriteriaBuilder cb, String field,
                                                       String from, String to) {
        Predicate p = null;
        if (StringUtils.isNotBlank(from)) {
            try {
                BigDecimal f = new BigDecimal(from.trim());
                Path<BigDecimal> path = root.get(field);
                Predicate gte = cb.greaterThanOrEqualTo(path, f);
                p = (p == null) ? gte : cb.and(p, gte);
            } catch (NumberFormatException ignored) {}
        }
        if (StringUtils.isNotBlank(to)) {
            try {
                BigDecimal t = new BigDecimal(to.trim());
                Path<BigDecimal> path = root.get(field);
                Predicate lte = cb.lessThanOrEqualTo(path, t);
                p = (p == null) ? lte : cb.and(p, lte);
            } catch (NumberFormatException ignored) {}
        }
        return p;
    }

    public static Predicate buildDateRangePredicate(Root<?> root, CriteriaBuilder cb, String field,
                                                    String from, String to) {
        Predicate p = null;
        if (StringUtils.isNotBlank(from)) {
            Timestamp start = parseToStartOfDay(from.trim());
            if (start != null) {
                Path<Timestamp> path = root.get(field);
                Predicate gte = cb.greaterThanOrEqualTo(path, start);
                p = (p == null) ? gte : cb.and(p, gte);
            }
        }
        if (StringUtils.isNotBlank(to)) {
            Timestamp end = parseToEndOfDay(to.trim());
            if (end != null) {
                Path<Timestamp> path = root.get(field);
                Predicate lte = cb.lessThanOrEqualTo(path, end);
                p = (p == null) ? lte : cb.and(p, lte);
            }
        }
        return p;
    }

    // no-op helpers removed

    private static Timestamp parseToStartOfDay(String text) {
        LocalDateTime ldt = parseDateOrDateTime(text);
        if (ldt == null) return null;
        return Timestamp.valueOf(ldt.withHour(0).withMinute(0).withSecond(0).withNano(0));

    }

    private static Timestamp parseToEndOfDay(String text) {
        LocalDateTime ldt = parseDateOrDateTime(text);
        if (ldt == null) return null;
        return Timestamp.valueOf(ldt.withHour(23).withMinute(59).withSecond(59).withNano(999_000_000));
    }

    private static LocalDateTime parseDateOrDateTime(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        try {
            // Try parsing as Persian/Jalali date first (format: yyyy/MM/dd or yyyy-MM-dd)
            Date persianDate = null;
            if (text.contains("/")) {
                // Persian date format: 1404/10/01
                try {
                    int year = Integer.parseInt(text.substring(0, 4));
                    if (year < 1900) {
                        // This is likely a Persian/Jalali date
                        persianDate = DateUtils.parse(text, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
                    }
                } catch (Exception ignored) {
                    // Not a Persian date, continue with Gregorian parsing
                }
            } else if (text.contains("-")) {
                // Try Persian date with dash separator
                try {
                    int year = Integer.parseInt(text.substring(0, 4));
                    if (year < 1900) {
                        persianDate = DateUtils.parse(text, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
                    }
                } catch (Exception ignored) {
                    // Not a Persian date, continue with Gregorian parsing
                }
            }

            if (persianDate != null) {
                // Convert Date to LocalDateTime
                return new java.sql.Timestamp(persianDate.getTime()).toLocalDateTime();
            }

            // Try parsing as Gregorian date
            if (text.length() <= 10) {
                LocalDate d = LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return d.atStartOfDay();
            }
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd['T'HH:mm[:ss]]"));
        } catch (DateTimeParseException e) {
            // If all parsing attempts fail, try DateUtils as last resort
            try {
                Date date = DateUtils.parse(text, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
                if (date != null) {
                    return new java.sql.Timestamp(date.getTime()).toLocalDateTime();
                }
            } catch (Exception ignored) {
                // Ignore and return null
            }
            return null;
        }
    }

}
