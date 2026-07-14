package com.sample.system.card.service.domain.valueObject;

/**
 * FeeProfile ID Value Object
 * Following DDD principles for value objects
 */
public class FeeProfileId extends BaseId<Long> {
    public FeeProfileId(Long value) {
        super(value);
    }
}
