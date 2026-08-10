package com.sample.system.card.service.domain.enums;

import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardRequestStatusTest {

    @ParameterizedTest
    @EnumSource(CardRequestStatus.class)
    void fromCodeReturnsTheMatchingStatus(CardRequestStatus status) {
        assertThat(CardRequestStatus.fromCode(status.getCode())).isEqualTo(status);
    }

    @Test
    void fromCodeRejectsNullAndUnknownCodes() {
        assertThatThrownBy(() -> CardRequestStatus.fromCode(null))
                .isInstanceOf(InvalidInputParameterException.class);
        assertThatThrownBy(() -> CardRequestStatus.fromCode(99))
                .isInstanceOf(InvalidInputParameterException.class);
    }

    @Test
    void busyStatusesExcludeTerminalStates() {
        assertThat(CardRequestStatus.busyStatuses())
                .doesNotContain(CardRequestStatus.FINISHED, CardRequestStatus.CANCELED)
                .contains(CardRequestStatus.INITIAL, CardRequestStatus.APPROVED);
    }

    @Test
    void initialRequestCanBeApprovedOrCanceled() {
        assertThat(CardRequestStatus.resolveNextStatuses(CardRequestStatus.INITIAL, null))
                .containsExactly(ActionRequestNextStatus.APPROVED, ActionRequestNextStatus.CANCELED);
    }

    @Test
    void issuedPhysicalCardGoesToPrinting() {
        CardProfile physical = CardProfile.builder().cardIssueMethod(CardIssueMethod.PHYSICAL).build();
        CardProfile virtual = CardProfile.builder().cardIssueMethod(CardIssueMethod.VIRTUAL).build();

        assertThat(CardRequestStatus.resolveNextStatuses(CardRequestStatus.ISSUED, physical))
                .containsExactly(ActionRequestNextStatus.PRINT_EXPECT);
        assertThat(CardRequestStatus.resolveNextStatuses(CardRequestStatus.ISSUED, virtual)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = CardRequestStatus.class, names = {"PRINTED", "FINISHED", "CANCELED"})
    void terminalStatusesHaveNoNextStep(CardRequestStatus status) {
        assertThat(CardRequestStatus.resolveNextStatuses(status, null)).isEmpty();
    }
}
