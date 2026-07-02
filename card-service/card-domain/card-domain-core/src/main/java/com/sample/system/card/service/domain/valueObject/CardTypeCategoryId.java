package com.sample.system.card.service.domain.valueObject;

/**
 * CardTypeCategory ID Value Object
 * Following DDD principles for value objects
 */
public class CardTypeCategoryId extends BaseId<Long> {
    public CardTypeCategoryId(Long value) {
        super(value);
    }
}
