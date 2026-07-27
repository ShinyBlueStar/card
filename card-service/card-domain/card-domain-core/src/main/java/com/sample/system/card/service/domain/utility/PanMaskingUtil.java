package com.sample.system.card.service.domain.utility;

/**
 * Utility class for masking PAN (Primary Account Number) for logging purposes
 * Following PCI DSS compliance - never log full PAN
 */
public final class PanMaskingUtil {

    private PanMaskingUtil() {
    }

    private static final String MASK_CHAR = "*";
    private static final int FIRST_VISIBLE_DIGITS = 6;
    private static final int LAST_VISIBLE_DIGITS = 4;

    /**
     * Masks a PAN for logging purposes
     * Format: First 6 digits visible, middle masked, last 4 digits visible
     * Example: 6037991234567890 -> 603799******7890
     *
     * @param pan The PAN to mask (can be null or empty)
     * @return Masked PAN string, or null if input is null/empty
     */
    public static String maskPan(String pan) {
        if (pan == null || pan.isBlank()) {
            return null;
        }

        String trimmedPan = pan.trim();
        int length = trimmedPan.length();

        // If PAN is too short, mask all but first and last character
        if (length <= FIRST_VISIBLE_DIGITS + LAST_VISIBLE_DIGITS) {
            if (length <= 2) {
                return MASK_CHAR.repeat(length);
            }
            return trimmedPan.charAt(0) + MASK_CHAR.repeat(length - 2) + trimmedPan.charAt(length - 1);
        }

        // Standard masking: show first 6, mask middle, show last 4
        String firstPart = trimmedPan.substring(0, FIRST_VISIBLE_DIGITS);
        String lastPart = trimmedPan.substring(length - LAST_VISIBLE_DIGITS);
        int maskedLength = length - FIRST_VISIBLE_DIGITS - LAST_VISIBLE_DIGITS;
        String maskedPart = MASK_CHAR.repeat(maskedLength);

        return firstPart + maskedPart + lastPart;
    }

}

