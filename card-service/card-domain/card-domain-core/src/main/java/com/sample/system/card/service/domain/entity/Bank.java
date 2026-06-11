package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.valueObject.BankId;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Bank Domain Entity
 * Aligned with BankCommandEntity (banks)
 * Represents bank information in the domain layer.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bank extends AggregateRoot<BankId> implements Serializable {

    private String binCode;        // کد BIN بانک (6 رقم)
    private String name;          // نام بانک
    private Boolean isActive;     // وضعیت فعال/غیرفعال
    private String createdBy;
    private Timestamp createdDate;
    private String lastModifiedBy;
    private Timestamp lastModifiedDate;

    /**
     * Check if bank is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Activate the bank
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate the bank
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Validate BIN code format
     */
    public boolean isValidBinCode() {
        return binCode != null && binCode.matches("\\d{6}");
    }
}
