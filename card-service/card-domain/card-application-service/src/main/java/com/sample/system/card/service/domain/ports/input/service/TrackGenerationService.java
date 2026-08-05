package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.exception.CardDomainException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Service interface for generating card tracks (Track1, Track2, Track3).
 * Following Hexagonal Architecture - Input Port.
 */
public interface TrackGenerationService extends BaseService {

    String generateTrack1(@NotBlank String pan, @NotBlank String expDate);

    /**
     * Generate Track 2 data (uses SSM for CVV1 when cardUuid is provided).
     * Format: {pan}={expDate}{serviceCode}...
     *
     * @param cardUuid Card UUID for SSM (e.g. for CVV1)
     * @param pan      16-digit card number
     * @param expDate  Expiration date (YYMM format)
     * @return Track 2 data
     */
    String generateTrack2(@NotNull UUID cardUuid, @NotBlank String pan, @NotBlank String expDate) throws CardDomainException;

    /**
     * Generate Track 3 data
     * Format: Variable based on card type and issuer requirements
     * @param pan 16-digit card number
     * @param additionalData Additional data for track 3
     * @return Track 3 data
     */
    String generateTrack3(@NotBlank String pan, String additionalData);
}

