package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.enums.*;
import com.sample.system.card.service.domain.valueObject.BankId;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import com.sample.system.card.service.domain.valueObject.CardTypeCategoryId;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.valueObject.FeeProfileId;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardProfile extends AggregateRoot<CardProfileId> implements Serializable {

    /** --- مشخصات پایه --- **/
    private String profileName;              // نام پروفایل کارت
    private CardType cardType;   // نوع کارت اعتباری (بانکی، کالایی و ...)
    private Long issuingBankId;            // شناسه بانک صادرکننده
    private Bank bank;
    private BankId bankId;
    private Long NumPatternId;
    private CardNumberPattern numPattern;
    private CardCategory cardCategory;
    private CardTypeCategory cardTypeCategory;
    private Long cardCategoryId;
    private Long cardTypeId;
    private List<CardNumberPattern> cardNumberRangeList;

    /** --- محدودیت‌ها و مقادیر کنترلی --- **/
    private Integer maxNumCreditCard;        // حداکثر کارت اعتباری
    private BigDecimal maximumAllowedAmount; // حداکثر مبلغ مجاز (ریال)
    private Integer minimumPasswordLength;   // حداقل طول رمز
    private Integer maximumPasswordLength;   // حداکثر طول رمز
    private Integer minimumSecondaryPasswordLength; // حداقل طول رمز دوم
    private Integer maximumSecondaryPasswordLength; // حداکثر طول رمز دوم
    private Integer allowedPinAttempts;      // دفعات مجاز تکرار PIN1
    private Integer allowedPin2Attempts;     // دفعات مجاز تکرار PIN2
    private Reloadable reloadable;              // قابلیت شارژ مجدد

    /** --- دوره اعتبار --- **/
    private Integer validityPeriod;          // دوره اعتبار کارت (ماه، سال، روز)
    private String validityType;             // نوع دوره (M=ماهانه، Y=سالانه و ...)

    /** --- سایر ویژگی‌ها --- **/
    private List<String> productCodeList;         // لیست کد محصولات (در DB به صورت عددی)
    private Integer allowedPinAttempt;         // دفعات مجاز تکرار رمز

    private Boolean initialCardStatus;         // وضعیت اولیه کارت (فعال، غیرفعال، ...)
    private SalesMethod salesMethod;// نحوه فروش (در شعبه، بدون محدودیت)
    private CardIssueMethod cardIssueMethod;
    private CardRenewalType cardRenewalType;             // نوع تمدید کارت

    /** --- رفتارهای منطقی --- **/
    private Boolean changeCvv2OnExpirationDateRenewal;   // تغییر CVV2 هنگام تمدید تاریخ انقضا
    private Boolean isActive;                           // فعال / غیرفعال
    private Boolean additionalInfoIsRequired;                   // اطلاعات تکمیلی روی کارت اجباری است

    /** --- رابطه با نوع و دسته‌بندی کارت --- **/
    private CardTypeCategoryId cardTypeCategoryId;       // شناسه رابطه نوع-دسته‌بندی کارت

    /** --- رابطه با پروفایل کارمزد --- **/
    private Long feeProfileIdLong;                    // شناسه پروفایل کارمزد (اختیاری)
    private FeeProfileId feeProfileId;                    // شناسه پروفایل کارمزد (اختیاری)
    private FeeProfile feeProfile;

    private Integer cvv2Length;

    private Pin2GenMethod pin2GenMethod;

    private String createdBy;
    private Timestamp createdDate;
    private String lastModifiedBy;
    private Timestamp lastModifiedDate;
    /**
     * Check if card profile is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Activate the card profile
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate the card profile
     */
    public void deactivate() {
        this.isActive = false;
    }

    public static void validateCardProfileCreation(Long cardCategoryId,
                                                   Long cardTypeId) throws CardDomainException {

        validateCardCategoryAndType(cardCategoryId, cardTypeId);
    }

    private static void validateCardCategoryAndType(Long cardCategoryId, Long cardTypeId) throws CardDomainException {
        if (cardCategoryId == null && cardTypeId == null) {
            return; // Both are optional
        }

        if (cardCategoryId == null) {
            throw new CardDomainException("در صورت ارسال شناسه نوع کارت، شناسه دسته‌بندی کارت نیز الزامی است");
        }

        if (cardTypeId == null) {
            throw new CardDomainException("در صورت ارسال شناسه دسته‌بندی کارت، شناسه نوع کارت نیز الزامی است");
        }
    }

    public void setValidityPeriod(Integer validityPeriod) throws CardDomainException {
        if(validityPeriod <1)
            throw new CardDomainException("Should be bigger than zero"
//                    , com.sample.system.card.service.domain.ports.input.service.StatusService.Validity_PERIOD_SHOULD_BE_BIGGER_THAN_ZERO
//                    , HttpStatus.BAD_REQUEST
            );
        this.validityPeriod = validityPeriod;
    }

    /**
     * Check if the update contains changes to immutable fields.
     * Immutable fields cannot be changed when cards are linked to this profile.
     */
    public boolean hasImmutableFieldChanges(
            String newProfileName,
            Long newIssuingBankId,
            Long newCardCategoryId,
            Long newCardTypeId,
            Long newFeeProfileId) {

        return (newProfileName != null && !Objects.equals(newProfileName, this.profileName))
            || (newIssuingBankId != null && !Objects.equals(newIssuingBankId, this.issuingBankId))
            || (newCardCategoryId != null && !Objects.equals(newCardCategoryId, this.cardCategoryId))
            || (newCardTypeId != null && !Objects.equals(newCardTypeId, this.cardTypeId))
            || (newFeeProfileId != null && !Objects.equals(newFeeProfileId, this.feeProfileIdLong));
    }

    public void updateWithLinkedCards(
            String profileName,
            Long issuingBankId,
            Long cardCategoryId,
            Long cardTypeId,
            Long feeProfileId,
            Integer minimumPasswordLength,
            Integer maximumPasswordLength,
            Integer minimumSecondaryPasswordLength,
            Integer maximumSecondaryPasswordLength,
            Integer allowedPinAttempts,
            Integer validityPeriod,
            String validityType,
            Integer maxNumCreditCard,
            BigDecimal maximumAllowedAmount,
            Reloadable reloadable,
            Boolean initialCardStatus,
            SalesMethod salesMethod,
            Integer cvv2Length,
            Pin2GenMethod pin2GenMethod,
            Boolean changeCvv2OnExpirationDateRenewal,
            Boolean additionalInfoIsRequired,
            CardRenewalType cardRenewalType,
            Integer allowedPin2Attempts) throws CardDomainException {

        // Check for immutable field changes
        if (hasImmutableFieldChanges(profileName, issuingBankId, cardCategoryId, cardTypeId, feeProfileId)) {
            throw new CardDomainException(
                "Cannot update immutable fields (profileName, issuingBankId, cardCategoryId, cardTypeId, feeProfileId) when cards are linked to this profile"
            );
        }

        // Update only mutable fields
        applySecurityAndPolicyChanges(
            minimumPasswordLength, maximumPasswordLength,
            minimumSecondaryPasswordLength, maximumSecondaryPasswordLength,
            allowedPinAttempts, validityPeriod, validityType,
            maxNumCreditCard, maximumAllowedAmount, reloadable,
            initialCardStatus, salesMethod, cvv2Length, pin2GenMethod,
            changeCvv2OnExpirationDateRenewal, additionalInfoIsRequired,
            cardRenewalType, allowedPin2Attempts
        );
    }

    public void updateWithoutLinkedCards(
            String profileName,
            Long issuingBankId,
            Long cardCategoryId,
            Long cardTypeId,
            Long feeProfileId,
            Integer minimumPasswordLength,
            Integer maximumPasswordLength,
            Integer minimumSecondaryPasswordLength,
            Integer maximumSecondaryPasswordLength,
            Integer allowedPinAttempts,
            Integer validityPeriod,
            String validityType,
            Integer maxNumCreditCard,
            BigDecimal maximumAllowedAmount,
            Reloadable reloadable,
            Boolean initialCardStatus,
            SalesMethod salesMethod,
            Integer cvv2Length,
            Pin2GenMethod pin2GenMethod,
            Boolean changeCvv2OnExpirationDateRenewal,
            Boolean additionalInfoIsRequired,
            CardRenewalType cardRenewalType,
            Integer allowedPin2Attempts) throws CardDomainException {

        // Validate card category and type if provided
        if (cardCategoryId != null || cardTypeId != null) {
            validateCardProfileCreation(cardCategoryId, cardTypeId);
        }

        // Update immutable fields
        if (profileName != null) {
            this.profileName = profileName;
        }
        if (issuingBankId != null) {
            this.issuingBankId = issuingBankId;
        }
        if (cardCategoryId != null) {
            this.cardCategoryId = cardCategoryId;
        }
        if (cardTypeId != null) {
            this.cardTypeId = cardTypeId;
        }
        if (feeProfileId != null) {
            this.feeProfileIdLong = feeProfileId;
        }

        // Update mutable fields
        applySecurityAndPolicyChanges(
            minimumPasswordLength, maximumPasswordLength,
            minimumSecondaryPasswordLength, maximumSecondaryPasswordLength,
            allowedPinAttempts, validityPeriod, validityType,
            maxNumCreditCard, maximumAllowedAmount, reloadable,
            initialCardStatus, salesMethod, cvv2Length, pin2GenMethod,
            changeCvv2OnExpirationDateRenewal, additionalInfoIsRequired,
            cardRenewalType, allowedPin2Attempts
        );
    }

    private void applySecurityAndPolicyChanges(
            Integer minPass,
            Integer maxPass,
            Integer minPass2,
            Integer maxPass2,
            Integer allowedPinAttempts,
            Integer validityPeriod,
            String validityType,
            Integer maxNumCreditCard,
            BigDecimal maximumAllowedAmount,
            Reloadable reloadable,
            Boolean initialCardStatus,
            SalesMethod salesMethod,
            Integer cvv2Length,
            Pin2GenMethod pin2GenMethod,
            Boolean changeCvv2OnExpirationDateRenewal,
            Boolean additionalInfoIsRequired,
            CardRenewalType cardRenewalType,
            Integer allowedPin2Attempts) throws CardDomainException {

        if (minPass != null) this.minimumPasswordLength = minPass;
        if (maxPass != null) this.maximumPasswordLength = maxPass;
        if (minPass2 != null) this.minimumSecondaryPasswordLength = minPass2;
        if (maxPass2 != null) this.maximumSecondaryPasswordLength = maxPass2;
        if (allowedPinAttempts != null) this.allowedPinAttempts = allowedPinAttempts;
        if (validityPeriod != null) setValidityPeriod(validityPeriod);
        if (validityType != null) this.validityType = validityType;
        if (maxNumCreditCard != null) this.maxNumCreditCard = maxNumCreditCard;
        if (maximumAllowedAmount != null) this.maximumAllowedAmount = maximumAllowedAmount;
        if (reloadable != null) this.reloadable = reloadable;
        if (initialCardStatus != null) this.initialCardStatus = initialCardStatus;
        if (salesMethod != null) this.salesMethod = salesMethod;
        if (cvv2Length != null) this.cvv2Length = cvv2Length;
        if (pin2GenMethod != null) this.pin2GenMethod = pin2GenMethod;
        if (changeCvv2OnExpirationDateRenewal != null)
            this.changeCvv2OnExpirationDateRenewal = changeCvv2OnExpirationDateRenewal;
        if (additionalInfoIsRequired != null)
            this.additionalInfoIsRequired = additionalInfoIsRequired;
        if (cardRenewalType != null) this.cardRenewalType = cardRenewalType;
        if (allowedPin2Attempts != null) this.allowedPin2Attempts = allowedPin2Attempts;
    }

    public void appendPattern(CardNumberPattern pattern) {
        if (this.cardNumberRangeList == null) {
            this.cardNumberRangeList = new ArrayList<>();
        }

        // Check if pattern already exists
        boolean alreadyExists = this.cardNumberRangeList.stream()
                .anyMatch(p -> p.getId() != null &&
                        pattern.getId() != null &&
                        p.getId().equals(pattern.getId()));

        if (!alreadyExists) {
            this.cardNumberRangeList.add(pattern);
            this.NumPatternId = pattern.getId() != null ? pattern.getId().getValue() : null;
        }
    }

    public void replacePattern(CardNumberPattern pattern) {
        this.numPattern = pattern;
        this.NumPatternId = pattern.getId() != null ? pattern.getId().getValue() : null;
        // Use ArrayList instead of List.of() for JPA compatibility
        this.cardNumberRangeList = new ArrayList<>();
        if (pattern != null) {
            this.cardNumberRangeList.add(pattern);
        }
    }
}