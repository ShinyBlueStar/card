package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.valueObject.CardCategoryId;
import com.sample.system.card.service.domain.valueObject.CardTypeCategoryId;
import com.sample.system.card.service.domain.valueObject.CardTypeId;
import lombok.*;

import java.io.Serializable;

/**
 * CardTypeCategory Domain Entity - Intermediate Entity
 * Represents the relationship between CardType and CardCategory
 * Following DDD principles with relationship-specific attributes
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTypeCategory extends AggregateRoot<CardTypeCategoryId> implements Serializable {

    private CardTypeId cardTypeId;        // شناسه نوع کارت
    private CardCategoryId cardCategoryId; // شناسه دسته‌بندی کارت
    private Boolean isActive;             // وضعیت فعال/غیرفعال
    private CardType cardType;
    private CardCategory cardCategory;
    private Integer priority;
    private String description;

    /**
     * Check if this relationship is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Activate this relationship
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate this relationship
     */
    public void deactivate() {
        this.isActive = false;
    }

}
