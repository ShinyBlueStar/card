package com.sample.system.card.service.domain.valueObject;

/**
 * Bank ID Value Object
 * Following DDD principles for value objects
 */
public class BankId extends BaseId<Long> {
    public BankId(Long value) {
        super(value);
    }
}
