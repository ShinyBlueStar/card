package com.sample.system.card.service.domain.valueObject;

/**
 * CardType ID Value Object
 * Following DDD principles for value objects
 */
public class CardTypeId extends BaseId<Long> {
    public CardTypeId(Long value) {
        super(value);
    }
}
