package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.enums.CardPhysicalStatus;
import com.sample.system.card.service.domain.enums.CardStatus;
import com.sample.system.card.service.domain.valueObject.CardId;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card extends AggregateRoot<CardId> implements Serializable {

    private  String nationalId;
    /** Unique card identifier (UUID) for SSM integration; generated on card creation. */
    private  UUID cardUuid;
    private  LocalDateTime issueDate;
    private  CardProfileId cardProfileId;   // رابطه با CardProfile
    private  CardProfile cardProfile;   // رابطه با CardProfile
    private  CardStatus cardStatus;
    private CardPhysicalStatus cardPhysicalStatus;
    private  String caseNumber;
    private  String pan;                    // Full PAN (16 digits)
    private  String maskedPan;              // Masked PAN for logging (PCI DSS compliance)
    private  String cvv2;
    private  String nameFr;  // نام فارسی
    private  String nameEn;  // نام انگلیسی
    private  String expDate;            // تاریخ انقضا (رaw)
    private  String firstStatementDate; // تاریخ اولین صورتحساب
    private  LocalDateTime receivedDate;
    private  LocalDateTime sentDate;

    // ====================== واحد عرضه کننده ======================
    private  String unitCode;
    private  String unitName;
    private  String issuingBank;

    // ====================== حساب / مشتری ======================
    private  String cifNo;   // Customer info number
    private  String crn;
    private  String crmNo;
    private  String accountType;
    private  String cardAccountGroupName;

    // ====================== تراک‌ها ======================
    private Track track;

    // ====================== اعتبار / آدرس ======================
    private  BigDecimal creditLimit;
    private  String address;

    // ====================== کنترل / امنیت ======================
    private  Integer wrongPassRetryCount;
    private  Boolean isSecondPassHot;
    private  Integer wrongSecondPasswordRetryCount;
    private  Boolean isVirtualCard;

    // ====================== سایر ======================
    private  String macnaCode;
    private  String bonCardText;
    private  String deliveryPersonId;
    private  String issuerPersonId;
    private  String changeStateDescription;

    private  String createdBy;
    private  Timestamp createdDate;
    private  String lastModifiedBy;
    private  Timestamp lastModifiedDate;

    // ====================== Customer ======================
    private Customer customer;  // Reference to Customer entity
    private Long oldCardId;
    // ====================== رفتارهای کمکی ======================

    /**
     * بررسی اینکه کارت مجازی است یا فیزیکی
     */
    public boolean isVirtual() {
        return Boolean.TRUE.equals(isVirtualCard);
    }

    /**
     * بررسی اینکه کارت هنوز فعال است یا خیر
     */
    public boolean isActive() {
        return true;
    }

    public String getCvv2(String PAN){
        //get from ssm
        return PAN.substring(16, 20);
    }
}