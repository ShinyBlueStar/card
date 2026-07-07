package com.sample.system.card.service.domain.response.profile;

import com.sample.system.card.service.domain.enums.*;
import com.sample.system.card.service.domain.response.bank.BankResponse;
import com.sample.system.card.service.domain.response.base.EnumResponse;
import com.sample.system.card.service.domain.response.cardCategory.GetCardCategoryResponse;
import com.sample.system.card.service.domain.response.cardType.GetCardTypeResponse;
import com.sample.system.card.service.domain.response.feeProfile.FeeProfileResponse;
import com.sample.system.card.service.domain.response.number.GetCardNumberPatternResponse;

import java.math.BigDecimal;
import java.util.List;

public record GetCardProfileResponse(
        Long id,
        String profileName,
        String cardType,
        GetCardTypeResponse cardTypeResponse,
        GetCardCategoryResponse cardCategoryResponse,
        BankResponse issuingBank,
        FeeProfileResponse feeProfile,
        List<GetCardNumberPatternResponse> cardNumberRangeList,
        Integer maxNumCreditCard,
        BigDecimal maximumAllowedAmount,
        Integer minimumPasswordLength,
        Integer maximumPasswordLength,
        Integer minimumSecondaryPasswordLength,
        Integer maximumSecondaryPasswordLength,
        Integer allowedPinAttempts,
        Integer allowedPin2Attempts,
        EnumResponse reloadable,
        Integer validityPeriod,
        String validityType,
        EnumResponse cardRenewalType,
//        List<String> productCodeList,
        Boolean initialCardStatus,
        EnumResponse salesMethod,
        Boolean changeCvv2OnExpirationDateRenewal,
        Integer cvv2Length,
        Boolean isActive,
        Boolean additionalInfoIsRequired,
        EnumResponse pin2GenMethod,
        String createdBy,
        String createdDate,
        String lastModifiedBy,
        String lastModifiedDate,
        String message,
        EnumResponse issueMethod) {
}
