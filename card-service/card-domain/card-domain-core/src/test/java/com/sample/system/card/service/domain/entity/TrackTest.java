package com.sample.system.card.service.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrackTest {

    @Test
    void emptyTrackIsNeitherValidNorSecure() {
        Track track = Track.builder().track1(" ").build();

        assertThat(track.isValid()).isFalse();
        assertThat(track.getTrackCount()).isZero();
        assertThat(track.isSecure()).isFalse();
    }

    @Test
    void countsOnlyNonBlankTracks() {
        Track track = Track.builder().track1("T1").track2("").track3("T3").build();

        assertThat(track.isValid()).isTrue();
        assertThat(track.getTrackCount()).isEqualTo(2);
    }

    @Test
    void secureWhenTrackAndAnyPinArePresent() {
        assertThat(Track.builder().track2("T2").pin2("1234").build().isSecure()).isTrue();
        assertThat(Track.builder().track2("T2").build().isSecure()).isFalse();
    }
}
