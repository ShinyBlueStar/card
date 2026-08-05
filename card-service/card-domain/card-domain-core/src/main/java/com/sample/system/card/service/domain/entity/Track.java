package com.sample.system.card.service.domain.entity;

import lombok.*;

import java.io.Serializable;

/**
 * Track Domain Entity
 * Mirrors TrackCommandEntity: track1/2/3, pin1/2, and relation to Card
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Track extends BaseEntity<Long> implements Serializable {

    /** Track 1 data */
    private final String track1;

    /** Track 2 data */
    private final String track2;

    /** Track 3 data */
    private final String track3;

    /** Primary PIN */
    private final String pin1;

    /** Secondary PIN */
    private final String pin2;

    private final String pin1part2;

    private final String pin2part2;

    /** Associated Card (1:1) */
    private final Card card;

    // -----------------------------
    // Domain behaviors (methods)
    // -----------------------------

    /** بررسی معتبر بودن Track */
    public boolean isValid() {
        return (track1 != null && !track1.isBlank()) ||
                (track2 != null && !track2.isBlank()) ||
                (track3 != null && !track3.isBlank());
    }

    /** بررسی وجود PIN اولیه */
    public boolean hasPrimaryPin() {
        return pin1 != null && !pin1.isBlank();
    }

    /** بررسی وجود PIN ثانویه */
    public boolean hasSecondaryPin() {
        return pin2 != null && !pin2.isBlank();
    }

    /** دریافت تعداد Track های موجود */
    public int getTrackCount() {
        int count = 0;
        if (track1 != null && !track1.isBlank()) count++;
        if (track2 != null && !track2.isBlank()) count++;
        if (track3 != null && !track3.isBlank()) count++;
        return count;
    }

    /** بررسی امنیت Track ها */
    public boolean isSecure() {
        return isValid() && (hasPrimaryPin() || hasSecondaryPin());
    }

    // Allow mappers to set identifier after build
    public void setId(Long id) {
        super.setId(id);
    }
}
