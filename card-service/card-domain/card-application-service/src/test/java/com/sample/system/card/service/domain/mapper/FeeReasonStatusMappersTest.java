package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.CreateStatusCommand;
import com.sample.system.card.service.domain.command.UpdateStatusCommand;
import com.sample.system.card.service.domain.command.feeProfile.CreateFeeProfileCommand;
import com.sample.system.card.service.domain.command.reason.CreateReasonCommand;
import com.sample.system.card.service.domain.entity.Fee;
import com.sample.system.card.service.domain.entity.FeeProfile;
import com.sample.system.card.service.domain.entity.Reason;
import com.sample.system.card.service.domain.entity.Status;
import com.sample.system.card.service.domain.enums.ReasonGroup;
import com.sample.system.card.service.domain.response.*;
import com.sample.system.card.service.domain.response.feeProfile.*;
import com.sample.system.card.service.domain.response.reason.*;
import com.sample.system.card.service.domain.valueObject.FeeProfileId;
import com.sample.system.card.service.domain.valueObject.ReasonId;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeeReasonStatusMappersTest {

    private final FeeProfileDataMapper feeMapper = new FeeProfileDataMapper();
    private final ReasonDataMapper reasonMapper = new ReasonDataMapper();
    private final StatusDataMapper statusMapper = new StatusDataMapper();

    private static FeeProfile profile(Long id, String name, Boolean active) {
        FeeProfile profile = new FeeProfile();
        if (id != null) {
            profile.setId(new FeeProfileId(id));
        }
        profile.setName(name);
        profile.setIsActive(active);
        return profile;
    }

    private static Reason reason(Long id) {
        Reason reason = Reason.builder().code(5).title("Lost").reason("Card lost").groupId(ReasonGroup.BLOCK_CARD).build();
        if (id != null) {
            reason.setId(new ReasonId(id));
        }
        return reason;
    }

    // ---------- fee profile ----------

    @Test
    void createWithNameStartsWithEmptyFees() {
        FeeProfile profile = feeMapper.create("Basic");

        assertThat(profile.getName()).isEqualTo("Basic");
        assertThat(profile.getFees()).isEmpty();
    }

    @Test
    void createCommandDefaultsToActiveWhenFlagMissing() {
        assertThat(feeMapper.createCommandToFeeProfile(new CreateFeeProfileCommand("Basic", "d", null)).getIsActive()).isTrue();
        assertThat(feeMapper.createCommandToFeeProfile(new CreateFeeProfileCommand("Basic", "d", false)).getIsActive()).isFalse();
        assertThat(feeMapper.createCommandToFeeProfile(new CreateFeeProfileCommand("Basic", "d", true)).getName()).isEqualTo("Basic");
    }

    @Test
    void updateChangesNameOnlyWhenProvided() {
        FeeProfile profile = profile(1L, "Old", true);

        feeMapper.update(profile, null);
        assertThat(profile.getName()).isEqualTo("Old");

        feeMapper.update(profile, "New");
        assertThat(profile.getName()).isEqualTo("New");
    }

    @Test
    void feesFallBackToEmptyListWhenNull() {
        FeeProfile profile = profile(1L, "p", true);
        Fee fee = new Fee();
        profile.setFees(new ArrayList<>(List.of(fee)));
        assertThat(feeMapper.fees(profile)).containsExactly(fee);

        profile.setFees(null);
        assertThat(feeMapper.fees(profile)).isEmpty();
    }

    @Test
    void feeProfileIsMappedToCreateAndGetResponses() {
        CreateFeeProfileResponse created = feeMapper.feeProfileToCreateResponse(profile(4L, "p", true), "ok");
        GetFeeProfileResponse got = feeMapper.feeProfileToGetResponse(profile(4L, "p", true));

        assertThat(created.getFeeProfileId()).isEqualTo(4L);
        assertThat(created.getName()).isEqualTo("p");
        assertThat(created.getMessage()).isEqualTo("ok");
        assertThat(created.getDescription()).isNull();
        assertThat(got.id()).isEqualTo(4L);
        assertThat(got.isActive()).isTrue();
    }

    @Test
    void feeProfileWithoutIdIsMappedWithNullId() {
        assertThat(feeMapper.feeProfileToCreateResponse(profile(null, "p", true), "m").getFeeProfileId()).isNull();
        assertThat(feeMapper.feeProfileToGetResponse(profile(null, "p", true)).id()).isNull();
    }

    @Test
    void feeProfileListsAndPagesAreMapped() {
        assertThat(feeMapper.feeProfilesToGetResponses(List.of(profile(1L, "a", true), profile(2L, "b", false)))).hasSize(2);

        FeeProfileListResponse page = feeMapper.feeProfilesToGetListResponse(
                new PageImpl<>(List.of(profile(1L, "a", true)), PageRequest.of(1, 1), 4));
        assertThat(page.getList()).hasSize(1);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(4);
        assertThat(feeMapper.feeProfilesToGetListResponse(null)).isNull();
    }

    // ---------- reason ----------

    @Test
    void reasonCommandIsMappedToReason() {
        Reason reason = reasonMapper.createCommandToReason(new CreateReasonCommand(9, "Stolen", "desc", ReasonGroup.UNBLOCK_CARD));

        assertThat(reason.getCode()).isEqualTo(9);
        assertThat(reason.getTitle()).isEqualTo("Stolen");
        assertThat(reason.getReason()).isEqualTo("desc");
        assertThat(reason.getGroupId()).isEqualTo(ReasonGroup.UNBLOCK_CARD);
    }

    @Test
    void reasonIsMappedToResponsesWithAndWithoutId() {
        CreateReasonResponse created = reasonMapper.reasonToCreateResponse(reason(3L), "done");
        GetReasonResponse got = reasonMapper.reasonToGetResponse(reason(3L));

        assertThat(created.getReasonId()).isEqualTo(3L);
        assertThat(created.getCode()).isEqualTo(5);
        assertThat(created.getTitle()).isEqualTo("Lost");
        assertThat(created.getReason()).isEqualTo("Card lost");
        assertThat(created.getGroupId()).isEqualTo(ReasonGroup.BLOCK_CARD);
        assertThat(created.getMessage()).isEqualTo("done");
        assertThat(got.id()).isEqualTo(3L);
        assertThat(reasonMapper.reasonToCreateResponse(reason(null), "m").getReasonId()).isNull();
        assertThat(reasonMapper.reasonToGetResponse(reason(null)).id()).isNull();
    }

    @Test
    void reasonPageIsMappedWithPagingInfo() {
        ReasonListResponse response = reasonMapper.reasonsToGetResponses(
                new PageImpl<>(List.of(reason(1L)), PageRequest.of(0, 5), 11));

        assertThat(response.getList()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(11);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(reasonMapper.reasonsToGetResponses(null)).isNull();
    }

    @Test
    void reasonListIsMappedAsSinglePage() {
        ReasonListResponse response = reasonMapper.reasonListToGetResponses(List.of(reason(1L), reason(2L)));

        assertThat(response.getList()).hasSize(2);
        assertThat(response.getNumber()).isZero();
        assertThat(response.getSize()).isEqualTo(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(1);
    }

    @Test
    void emptyReasonListHasZeroPages() {
        ReasonListResponse response = reasonMapper.reasonListToGetResponses(List.of());

        assertThat(response.getTotalPages()).isZero();
        assertThat(reasonMapper.reasonListToGetResponses(null)).isNull();
    }

    // ---------- status ----------

    @Test
    void statusCommandsAreMapped() {
        Status created = statusMapper.createCommandToStatus(new CreateStatusCommand("C1", "English", "فارسی"));
        Status updated = statusMapper.updateCommandToStatus(new UpdateStatusCommand("C1", "جدید"));

        assertThat(created.getCode()).isEqualTo("C1");
        assertThat(created.getDescription()).isEqualTo("English");
        assertThat(created.getPersianDescription()).isEqualTo("فارسی");
        assertThat(updated.getCode()).isEqualTo("C1");
        assertThat(updated.getPersianDescription()).isEqualTo("جدید");
        assertThat(updated.getDescription()).isNull();
    }

    @Test
    void statusIsMappedToResponses() {
        Status status = new Status("فارسی", "C1", "English");
        status.setId(8L);

        CreateStatusResponse created = statusMapper.statusToCreateResponse(status, "ok");
        UpdateStatusResponse updated = statusMapper.statusToUpdateResponse(status, "upd");
        GetStatusResponse got = statusMapper.statusToGetResponse(status);

        assertThat(created.getStatusId()).isEqualTo(8L);
        assertThat(created.getMessage()).isEqualTo("ok");
        assertThat(updated.getStatusId()).isEqualTo(8L);
        assertThat(updated.getCode()).isEqualTo("C1");
        assertThat(updated.getPersianDescription()).isEqualTo("فارسی");
        assertThat(updated.getMessage()).isEqualTo("upd");
        assertThat(got.getId()).isEqualTo(8L);
        assertThat(got.getDescription()).isEqualTo("English");
        assertThat(got.getMessage()).isNull();
    }

    @Test
    void statusPageIsMapped() {
        Status status = new Status("p", "C1", "d");
        status.setId(1L);

        StatusListResponse response = statusMapper.statusesToGetListResponse(
                new PageImpl<>(List.of(status), PageRequest.of(0, 1), 2));

        assertThat(response.getList()).hasSize(1);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(statusMapper.statusesToGetListResponse(null)).isNull();
    }
}
