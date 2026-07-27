package com.sample.system.card.service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * CardStatus Value Object
 * Represents the possible states of a card
 * Following DDD principles for value objects
 */
@Getter
@RequiredArgsConstructor
public enum CardStatus implements CodedEnum {
    ISSUED(1, "صادر شده"),
    ACTIVE(2,"فعال"),
    INACTIVE(3,"غیرفعال"),
    HOT(4,"مسدود"),
    EXPIRED(5,"منقضی شده");
//    PENDING(,"در انتظار"),
//    SUSPENDED(6,"معلق");

    private final Integer code;
    private final String description;

    public static CardStatus fromCode(int code) {
        for (CardStatus status : values()) {
            if (status.code == code)
                return status;
        }
        throw new IllegalArgumentException("کد وضعیت کارت نامعتبر است: " + code);
    }

    public boolean canNotBeActivated() {
        return this == INACTIVE || this == EXPIRED;
    }
    /**
     * Check if card can be blocked
     */
    public boolean canBeBlocked() {
        return this == ACTIVE;
    }

    /**
     * Check if card is in a terminal state
     */
    public boolean isTerminalState() {
        return this == EXPIRED || this == INACTIVE;
    }
}
