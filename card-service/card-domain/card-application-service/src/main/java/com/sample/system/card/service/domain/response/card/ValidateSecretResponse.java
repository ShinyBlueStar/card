package com.sample.system.card.service.domain.response.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response for validate/verify operations (PIN/OTP/CVV2)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateSecretResponse {
    private boolean valid;
    private String message;
}
