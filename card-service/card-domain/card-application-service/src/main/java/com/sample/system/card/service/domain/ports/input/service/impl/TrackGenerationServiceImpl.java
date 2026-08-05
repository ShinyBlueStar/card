package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.TrackGenerationService;
import com.sample.system.card.service.domain.ports.output.externalService.SSMService;
import com.sample.system.card.service.domain.utility.PanMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of TrackGenerationService
 * Generates Track1, Track2, and Track3 data according to ISO/IEC 7813 standards
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackGenerationServiceImpl implements TrackGenerationService {

    private static final String TRACK1_START_SENTINEL = "B";
    private static final String TRACK1_FIELD_SEPARATOR = "^";
    private static final String TRACK1_NAME_SEPARATOR = "/";
    private static final String TRACK1_END_SENTINEL = "?";
    private static final String TRACK2_FIELD_SEPARATOR = "=";
    private static final String TRACK2_END_SENTINEL = "?";
    private static final String TRACK3_START_SENTINEL = "01";
    private static final String TRACK3_FIELD_SEPARATOR = "=";
    private static final String TRACK3_END_SENTINEL = "?";
    private static final String DEFAULT_SERVICE_CODE = "120"; // Service code as per specification
    private static final int TRACK1_MAX_LENGTH = 79;
    private static final int TRACK2_MAX_LENGTH = 40;
    private static final int TRACK3_MAX_LENGTH = 107; // ISO/IEC 7813 standard max length for Track3
    private static final int TARGET_LENGTH = 13; // ISO/IEC 7813 standard max length for Track3
    private final SSMService ssmService;
    @Override
    public String generateTrack1(String pan, String expDate) {
        log.debug("Generating Track1 for PAN: {}, expDate: {}", PanMaskingUtil.maskPan(pan), expDate);

        if (pan == null || pan.length() != 16) {
            throw new IllegalArgumentException("PAN must be exactly 16 digits");
        }

        if (expDate == null || expDate.length() != 4) {
            throw new IllegalArgumentException("Expiration date must be exactly 4 digits (YYMM format)");
        }

        // Format according to specification:
        // %B{pan}^{firstName}/{lastName}^{expDate}{serviceCode}...
        // Structure: Template Code (B) + PAN (16) + ^ + Name with / + ^ + ExpDate (4) + ServiceCode (3)
        StringBuilder track1 = new StringBuilder();

        // Template Code: "B" (1 character, fixed)
        track1.append(TRACK1_START_SENTINEL); // %B

        // Card Number: 16 characters (variable)
        track1.append(pan);

        // Separator: "^" (1 character, fixed)
        track1.append(TRACK1_FIELD_SEPARATOR);

        // Separator: "^" (1 character, fixed)
        track1.append(TRACK1_NAME_SEPARATOR);
        track1.append(TRACK1_FIELD_SEPARATOR);

        // Expiration Date: 4 characters (variable, YYMM format)
        track1.append(expDate);

        // Service Code: 3 characters (fixed "120")
        track1.append(DEFAULT_SERVICE_CODE);

        String track1Data = track1.toString();
        // Ensure max length
        if (track1Data.length() > TRACK1_MAX_LENGTH) {
            track1Data = track1Data.substring(0, TRACK1_MAX_LENGTH - 1) + TRACK1_END_SENTINEL;
        }

        log.debug("Generated Track1: {}", track1Data);
        return track1Data;
    }

    @Override
    public String generateTrack2(UUID cardUuid, String pan, String expDate) throws CardDomainException {
        log.info("Generating Track2 for cardUuid={}, PAN={}, expDate={}", cardUuid, PanMaskingUtil.maskPan(pan), expDate);

        if (pan == null || pan.length() != 16) {
            throw new IllegalArgumentException("PAN must be exactly 16 digits");
        }

        if (expDate == null || expDate.length() != 4) {
            throw new IllegalArgumentException("Expiration date must be exactly 4 digits (YYMM format)");
        }

        StringBuilder track2 = new StringBuilder();
        track2.append(pan);
        track2.append(TRACK2_FIELD_SEPARATOR);
        track2.append(expDate);
        track2.append(DEFAULT_SERVICE_CODE);

        String cvv1 = ssmService.getCvv2(cardUuid, pan,(Integer)null,expDate, DEFAULT_SERVICE_CODE);
        int length = cvv1.length();
        int newLength = 13 - length;

        if (cvv1 == null) {
            throw new IllegalStateException("CVV1 generation returned null");
        }

        if (cvv1.length() > TARGET_LENGTH) {
            throw new IllegalStateException(
                    "CVV1 length exceeds allowed track segment length: " + cvv1.length()
            );
        }
        track2.append(cvv1);
        int remainingLength = TARGET_LENGTH - cvv1.length();
        track2.append("0".repeat(remainingLength));

        String track2Data = track2.toString();
        // Ensure max length
        if (track2Data.length() > TRACK2_MAX_LENGTH) {
            track2Data = track2Data.substring(0, TRACK2_MAX_LENGTH - 1) + TRACK2_END_SENTINEL;
        }

        log.debug("Generated Track2: {}", track2Data);
        return track2Data;
    }

    @Override
    public String generateTrack3(String pan, String additionalData) {
        log.debug("Generating Track3 for PAN: {}, additionalData: {}", PanMaskingUtil.maskPan(pan), additionalData);

        if (pan == null || pan.length() != 16) {
            throw new IllegalArgumentException("PAN must be exactly 16 digits");
        }

        // Track3 format according to ISO/IEC 7813 standards
        // Format: ;{formatCode}{pan}={expDate}{serviceCode}{additionalData}?
        // Track3 is typically used for financial transactions and contains:
        // - Format code
        // - PAN
        // - Expiration date
        // - Service restrictions
        // - Currency code
        // - Amount limits
        // - PIN verification value
        // - Additional discretionary data
        StringBuilder track3 = new StringBuilder();

        // Start sentinel: ";" (1 character, fixed)
        track3.append(TRACK3_START_SENTINEL);

        // Card Number: 16 characters (variable)
        track3.append(pan);

        // Separator: "=" (1 character, fixed)
        track3.append(TRACK3_FIELD_SEPARATOR);

        // Additional data if provided (can include expiration date, service code, etc.)
        if (additionalData != null && !additionalData.isBlank()) {
            track3.append(additionalData);
        } else {
            // Default: pad with zeros for standard format
            // This can be customized based on specific requirements
            track3.append("==0000000000000000000=0000000=1==1=");
        }

        String track3Data = track3.toString();
        // Ensure max length
        if (track3Data.length() > TRACK3_MAX_LENGTH) {
            track3Data = track3Data.substring(0, TRACK3_MAX_LENGTH - 1) + TRACK3_END_SENTINEL;
        }

        log.debug("Generated Track3: {}", track3Data);
        return track3Data;
    }
}

