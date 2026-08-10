package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.enums.CardRequestStatus;
import com.sample.system.card.service.domain.enums.CardRequestType;
import com.sample.system.card.service.domain.valueObject.CardId;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import com.sample.system.card.service.domain.valueObject.CardRequestId;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain Aggregate Root: CardRequest
 * Represents the lifecycle of a card issuance or maintenance request.
 * Persistence-agnostic and business-rule-focused.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CardRequest extends AggregateRoot<CardRequestId> implements Serializable {

    /** وضعیت درخواست */
    private CardRequestStatus requestStatus;

    /** نام کاربری ثبت‌کننده */
    private String requesterUsername;

    /** نوع درخواست */
    private CardRequestType requestType;

    /** علت درخواست */
    private Reason reason;

    private Long reasonId;

    /** شناسه پروفایل کارت مرتبط */
    private CardProfileId cardProfileId;

    /** شناسه پروفایل کارت (Long) - برای سهولت دسترسی */
    private Long profileId;

    /** شناسه کارت مرتبط */
    private CardId cardId;
    private Long oldCardId;

    private Card card;

    /** کد ملی متقاضی کارت */
    private String nationalId;

    /** شناسه شخصی صادرکننده / متقاضی */
    private String issuerPersonId;

    private Long lastRequestId;

    private String createdBy;

    private Timestamp createdDate;

    private String lastModifiedBy;

    private Timestamp lastModifiedDate;

    private String caseNumber;

    private String unitCode;

    private String unitName;

    private String firstName;

    private String lastName;

    private String unitId;

    private CardProfile cardProfile;
}
