package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.enums.ActionType;
import com.sample.system.card.service.domain.enums.FeeType;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Fee Domain Entity (DDD)
 * Mirrors fields of FeeCommandEntity in infrastructure layer without JPA concerns.
 */
public class Fee extends BaseEntity<Long> implements Serializable {

    private String name;

    private BigDecimal amount;

    private FeeProfile feeProfile;

    private FeeType feeType;

    private String feeValue;

    private ActionType actionType;

    private Integer feePeriod;

    private Boolean isActive;

    public Fee() {
    }

    public Fee(String name, BigDecimal amount, FeeType feeType, String feeValue,
               ActionType actionType, Integer feePeriod, Boolean isActive, FeeProfile feeProfile) {
        this.name = name;
        this.amount = amount;
        this.feeType = feeType;
        this.feeValue = feeValue;
        this.actionType = actionType;
        this.feePeriod = feePeriod;
        this.isActive = isActive;
        this.feeProfile = feeProfile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public FeeType getFeeType() {
        return feeType;
    }

    public void setFeeType(FeeType feeType) {
        this.feeType = feeType;
    }

    public String getFeeValue() {
        return feeValue;
    }

    public void setFeeValue(String feeValue) {
        this.feeValue = feeValue;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Integer getFeePeriod() {
        return feePeriod;
    }

    public void setFeePeriod(Integer feePeriod) {
        this.feePeriod = feePeriod;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public FeeProfile getFeeProfile() {
        return feeProfile;
    }

    public void setFeeProfile(FeeProfile feeProfile) {
        this.feeProfile = feeProfile;
    }

    public boolean isActive() {
        return isActive != null && isActive;
    }

    /**
     * Activate the fee
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate the fee
     */
    public void deactivate() {
        this.isActive = false;
    }
}