package com.sample.system.card.service.dataaccess.card.entity.command;

import com.sample.system.card.service.domain.enums.CardIssueMethod;
import com.sample.system.card.service.domain.enums.CardRenewalType;
import com.sample.system.card.service.domain.enums.Pin2GenMethod;
import com.sample.system.card.service.domain.enums.SalesMethod;
import com.sample.system.card.service.domain.enums.Reloadable;
import com.sample.system.card.service.dataaccess.card.entity.converter.EnumConverters;

import com.sample.system.card.service.dataaccess.card.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * CardType Command Entity for Infrastructure Layer
 * Represents the master data for card types with audit trail
 * Following DDD infrastructure patterns with One-to-Many relationship, Hibernate Envers and Spring Data JPA Auditing
 * Extends Auditable for automatic audit field population
 */
@Audited
@AuditOverride(forClass = Auditable.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "card_profiles")
public class CardProfileCommandEntity extends Auditable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_profile_seq")
    @SequenceGenerator(name = "card_profile_seq", sequenceName = "card_profile_seq", allocationSize = 1)
    private Long id;

    @Column(name = "NAME", length = 2000, unique = true, nullable = false)
    private String profileName;                        // نام پروفایل کارت

    @Column(name = "MAX_NUM_CREDIT_CARD")//?
    private Integer maxNumCreditCard;                  // حداکثر کارت اعتباری

    @Column(name = "MAXIMUM_ALLOWED_AMOUNT")//?
    private BigDecimal maximumAllowedAmount;                 // حداکثر مبلغ مجاز (ریال)

    @Column(name = "MIN_PASS_LENGTH")
    private Integer minimumPasswordLength;             // حداقل طول رمز

    @Column(name = "MAX_PASS_LENGTH")
    private Integer maximumPasswordLength;             // حداکثر طول رمز

    @Column(name = "MIN_2NDPASS_PASS_LENGTH")
    private Integer minimumSecondaryPasswordLength;    // حداقل طول رمز دوم

    @Column(name = "MAX_2NDPASS_PASS_LENGTH")
    private Integer maximumSecondaryPasswordLength;    // حداکثر طول رمز دوم

    @Column(name = "ALLOWED_PIN_ATTEMPTS")
    private Integer allowedPinAttempts;                // دفعات مجاز تکرار رمز

    @Column(name = "WRONG_PIN2_ATTEMPTS")
    private Integer allowedPin2Attempts;

    @Column(name = "RELOADABLE")
    @Convert(converter = EnumConverters.ReloadableConverter.class)
    private Reloadable reloadable; // قابلیت شارژ مجدد

    @Column(name = "VALIDITY_PERIOD")
    private Integer validityPeriod; // دوره اعتبار کارت (سال، ماه، روز)

    @Column(name = "VALIDITY_TYPE")
    private String validityType;

    @Builder.Default
    @ElementCollection
    @Column(name = "PRODUCTCODE_LIST")
    private List<String> productCodeList = new ArrayList<String>();// لیست کد محصول‌ها

    @Column(name = "INITIAL_CARD_STATUS")
    private Boolean initialCardStatus;

    @Column(name = "SALES_METHOD")
    @Convert(converter = EnumConverters.SalesMethodConverter.class)
    private SalesMethod salesMethod;      // نحوه فروش (در شعبه، بدون محدودیت)

    @Column(name = "CVV2_LENGTH")
    private Integer cvv2Length;

    @Column(name = "PIN2_GEN_METHOD")
    @Convert(converter = EnumConverters.Pin2GenMethodConverter.class)
    private Pin2GenMethod pin2GenMethod;

    @Column(name = "CARD_RENEWAL_TYPE", length = 100)
    @Convert(converter = EnumConverters.CardRenewalTypeConverter.class)
    private CardRenewalType cardRenewalType; // نوع تمدید کارت (بدون چاپ / با الزام چاپ)

    /** --- رفتارهای منطقی --- **/
    @Column(name = "CHANGE_CVV2_ON_EXPIRY_RENEWAL")
    private Boolean changeCvv2OnExpirationDateRenewal; // تغییر CVV2 هنگام تمدید تاریخ انقضا

    /** --- وضعیت و متادیتا --- **/
    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Column(name = "ADDITIONAL_INFO_IS_REQUIRED")
    private Boolean additionalInfoIsRequired;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ISSUING_BANK_ID", nullable = false)
    private BankCommandEntity issuingBank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_TYPE_CATEGORY_ID")
    private CardTypeCategoryCommandEntity cardTypeCategory;// ToDo bepors az hashemi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FEE_PROFILE_ID")
    private FeeProfileCommandEntity feeProfile;

    @Builder.Default
    @OneToMany(
            mappedBy = "cardProfile", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER
    )
    private List<CardNumberPatternCommandEntity> numberPatterns = new ArrayList<>();

    @Column(name = "ISSUE_METHOD")
    @Convert(converter = EnumConverters.CardIssueMethodConverter.class)
    private CardIssueMethod cardIssueMethod;

}