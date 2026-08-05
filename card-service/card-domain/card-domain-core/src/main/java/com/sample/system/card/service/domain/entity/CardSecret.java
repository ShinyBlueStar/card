package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.enums.SecretType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Domain model for card secret operation context (generate/validate).
 * Used as input to CardSecretService and internal flows; not exposed to API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardSecret {

    private String pan;
    private SecretType secretType;
    /** Value to verify (for validate operation). */
    private String value;
    private String expTime;
    private String serviceCode;
    private String channel;
    private String sessionType;
    private String clientFingerprint;
    /** Resolved from PAN by service. */
    private UUID cardId;
    /** National ID from card (for SSM). */
    private String nationalId;
    /** Result of validate: whether verification succeeded. */
    private Boolean valid;
    /** Result of validate: message (e.g. "Verification succeeded"). */
    private String message;
    private String reqType;
    private String encryptedData;
    private String cvvData;
}
