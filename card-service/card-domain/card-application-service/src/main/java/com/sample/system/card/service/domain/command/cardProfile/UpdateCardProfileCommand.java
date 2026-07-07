package com.sample.system.card.service.domain.command.cardProfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sample.system.card.service.domain.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class UpdateCardProfileCommand {

    @NotNull(message = "id is required")
    @JsonProperty("id")
    @Schema(name = "id", description = "Unique identifier of the card profile")
    private final Long cardProfileId;

    private final String profileName;
    private final Long issuingBankId;
    private final Long cardNumberPatternId;
    private final Long feeProfileId;
    private final Long cardCategoryId;
    private final Long cardTypeId;
    private final Integer minimumPasswordLength;
    private final Integer maximumPasswordLength;
    private final Integer minimumSecondaryPasswordLength;
    private final Integer maximumSecondaryPasswordLength;
    private final Integer allowedPinAttempts;
    @Min(value = 1, message = "مدت اعتبار کارت باید بزرگ‌تر از صفر باشد")
    @Max(value = 120, message = "مدت اعتبار کارت باید حداکثر 120 ماه باشد")
    private final Integer validityPeriod; // cardValidityPeriod → validityPeriod
    private final String validityType;
    private final Boolean initialCardStatus;
    private final Boolean isActive;
    private final Integer cvv2Length;
    private final Boolean changeCvv2OnExpirationDateRenewal; // تغییر CVV2 هنگام تمدید تاریخ انقضا
    private final Boolean additionalInfoIsRequired;
    private final Integer Pin2GenMethod; //enum Pin2GenMethod
    //////////////////////////////////////////////////////
    @Schema(defaultValue = "null")
    private final Integer reloadable;//  قابلیت شارژ مجدد// Rechargeable → Boolean
    @Schema(defaultValue = "null")
    private final Integer allowedPin2Attempts; // دفعات مجاز تکرار PIN2
    @Schema(defaultValue = "null")
    private final Integer salesMethod;// نحوه فروش  SalesMethod
    @Schema(defaultValue = "null")
    private final Integer maxNumCreditCard; // حداکثر کارت اعتباری
    @Schema(defaultValue = "null")
    private final BigDecimal maximumAllowedAmount;//حداکثر مبلغ مجاز
    private final Integer IssueMethod;
    @Schema(defaultValue = "null", allowableValues = {"1", "2"})
    private final Integer cardRenewalType; // نوع تمدید کارت (کد enum CardRenewalType)
}
