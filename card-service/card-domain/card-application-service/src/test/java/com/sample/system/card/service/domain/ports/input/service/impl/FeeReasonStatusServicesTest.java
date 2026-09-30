package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.feeProfile.CreateFeeProfileCommand;
import com.sample.system.card.service.domain.command.feeProfile.GetAllFeeProfilesQuery;
import com.sample.system.card.service.domain.command.feeProfile.GetFeeProfileQuery;
import com.sample.system.card.service.domain.command.reason.CreateReasonCommand;
import com.sample.system.card.service.domain.command.reason.GetReasonQuery;
import com.sample.system.card.service.domain.entity.FeeProfile;
import com.sample.system.card.service.domain.entity.Reason;
import com.sample.system.card.service.domain.entity.Status;
import com.sample.system.card.service.domain.enums.ReasonGroup;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.FeeProfileDataMapper;
import com.sample.system.card.service.domain.mapper.ReasonDataMapper;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.FeeProfileRepository;
import com.sample.system.card.service.domain.ports.output.repository.ReasonRepository;
import com.sample.system.card.service.domain.ports.output.repository.StatusRepository;
import com.sample.system.card.service.domain.valueObject.FeeProfileId;
import com.sample.system.card.service.domain.valueObject.ReasonId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeeReasonStatusServicesTest {

    @Mock
    private FeeProfileRepository feeRepository;
    @Mock
    private ReasonRepository reasonRepository;
    @Mock
    private StatusRepository statusRepository;

    private FeeProfileServiceImpl feeService() {
        return new FeeProfileServiceImpl(feeRepository, new FeeProfileDataMapper());
    }

    private ReasonServiceImpl reasonService() {
        return new ReasonServiceImpl(reasonRepository, new ReasonDataMapper());
    }

    private StatusServiceImpl statusService() {
        return new StatusServiceImpl(statusRepository);
    }

    private static FeeProfile profile(long id) {
        FeeProfile profile = new FeeProfile();
        profile.setId(new FeeProfileId(id));
        profile.setName("Basic");
        return profile;
    }

    private static Reason reason(long id) {
        Reason reason = Reason.builder().code(1).title("t").reason("r").groupId(ReasonGroup.BLOCK_CARD).build();
        reason.setId(new ReasonId(id));
        return reason;
    }

    private static Status status(Long id, String code) {
        Status status = new Status("فارسی", code, "d");
        status.setId(id);
        return status;
    }

    private static void assertDomainError(Throwable t, int status, HttpStatus http) {
        assertThat(t).isInstanceOfSatisfying(CardDomainException.class, e -> {
            assertThat(e.getStatus()).isEqualTo(status);
            assertThat(e.getHttpStatus()).isEqualTo(http);
        });
    }

    // ---------- fee profile ----------

    @Test
    void createFeeProfileSavesNewProfile() throws CardDomainException {
        when(feeRepository.existsByName("Basic")).thenReturn(false);
        when(feeRepository.save(any(FeeProfile.class))).thenAnswer(i -> {
            FeeProfile p = i.getArgument(0);
            p.setId(new FeeProfileId(1L));
            return p;
        });

        var event = feeService().createFeeProfile(new CreateFeeProfileCommand("Basic", "d", true));

        assertThat(event.getFeeProfile().getId().getValue()).isEqualTo(1L);
    }

    @Test
    void createFeeProfileRejectsDuplicateName() {
        when(feeRepository.existsByName("Basic")).thenReturn(true);

        assertThatThrownBy(() -> feeService().createFeeProfile(new CreateFeeProfileCommand("Basic", "d", true)))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.BAD_REQUEST));
        verify(feeRepository, never()).save(any());
    }

    @Test
    void createFeeProfileFailsWhenSaveReturnsNull() {
        when(feeRepository.existsByName("Basic")).thenReturn(false);
        when(feeRepository.save(any(FeeProfile.class))).thenReturn(null);

        assertThatThrownBy(() -> feeService().createFeeProfile(new CreateFeeProfileCommand("Basic", "d", true)))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("Basic");
    }

    @Test
    void getFeeProfileFindsOrReportsNotFound() throws CardDomainException {
        FeeProfile existing = profile(2L);
        when(feeRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(feeRepository.findById(3L)).thenReturn(Optional.empty());

        assertThat(feeService().getFeeProfile(new GetFeeProfileQuery(2L))).isSameAs(existing);
        assertThatThrownBy(() -> feeService().getFeeProfile(new GetFeeProfileQuery(3L)))
                .satisfies(t -> assertDomainError(t, StatusService.FEE_PROFILE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Test
    void getAllFeeProfilesUsesActiveQueryOnlyWhenRequested() throws CardDomainException {
        when(feeRepository.findActiveFeeProfiles()).thenReturn(List.of(profile(1L)));
        when(feeRepository.findAll()).thenReturn(List.of(profile(1L), profile(2L)));

        assertThat(feeService().getAllFeeProfiles(new GetAllFeeProfilesQuery(true))).hasSize(1);
        assertThat(feeService().getAllFeeProfiles(new GetAllFeeProfilesQuery(false))).hasSize(2);
        assertThat(feeService().getAllFeeProfiles(new GetAllFeeProfilesQuery(null))).hasSize(2);
    }

    @Test
    void listFeeProfilesDelegatesToRepository() throws CardDomainException {
        Map<String, String> params = Map.of("a", "b");
        Page<FeeProfile> page = new PageImpl<>(List.of(profile(1L)));
        when(feeRepository.findAllProfiles(params)).thenReturn(page);

        assertThat(feeService().listFeeProfiles(params, "me", "ip")).isSameAs(page);
    }

    // ---------- reason ----------

    @Test
    void findReasonByIdFindsOrReportsNotFound() throws CardDomainException {
        Reason existing = reason(1L);
        when(reasonRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(reasonRepository.findById(2L)).thenReturn(Optional.empty());

        assertThat(reasonService().findById(1L)).isSameAs(existing);
        assertThatThrownBy(() -> reasonService().findById(2L))
                .satisfies(t -> assertDomainError(t, StatusService.REASON_NOT_FOUND, HttpStatus.BAD_REQUEST));
    }

    @Test
    void createReasonSavesNewReason() throws CardDomainException {
        when(reasonRepository.existsByCode(1)).thenReturn(false);
        when(reasonRepository.save(any(Reason.class))).thenAnswer(i -> {
            Reason saved = ((Reason) i.getArgument(0)).toBuilder().build();
            saved.setId(new ReasonId(7L));
            return saved;
        });

        var event = reasonService().createReason(new CreateReasonCommand(1, "t", "r", ReasonGroup.BLOCK_CARD));

        assertThat(event.getReason().getId().getValue()).isEqualTo(7L);
    }

    @Test
    void createReasonRejectsDuplicateCode() {
        when(reasonRepository.existsByCode(1)).thenReturn(true);

        assertThatThrownBy(() -> reasonService().createReason(new CreateReasonCommand(1, "t", "r", ReasonGroup.BLOCK_CARD)))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.BAD_REQUEST));
    }

    @Test
    void createReasonFailsWhenSaveReturnsNull() {
        when(reasonRepository.existsByCode(1)).thenReturn(false);
        when(reasonRepository.save(any(Reason.class))).thenReturn(null);

        assertThatThrownBy(() -> reasonService().createReason(new CreateReasonCommand(1, "t", "r", ReasonGroup.BLOCK_CARD)))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("Could not save Reason");
    }

    @Test
    void getReasonFindsOrReportsNotFound() throws CardDomainException {
        Reason existing = reason(1L);
        when(reasonRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(reasonRepository.findById(2L)).thenReturn(Optional.empty());

        assertThat(reasonService().getReason(new GetReasonQuery(1L))).isSameAs(existing);
        assertThatThrownBy(() -> reasonService().getReason(new GetReasonQuery(2L)))
                .satisfies(t -> assertDomainError(t, StatusService.REASON_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Test
    void getAllReasonsDelegatesAndPropagatesFailures() throws CardDomainException {
        Map<String, String> params = Map.of();
        Page<Reason> page = new PageImpl<>(List.of(reason(1L)));
        when(reasonRepository.findAll(params)).thenReturn(page).thenThrow(new CardDomainException("db down"));

        assertThat(reasonService().getAllReasons(params, "me", "ip")).isSameAs(page);
        assertThatThrownBy(() -> reasonService().getAllReasons(params, "me", "ip"))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("db down");
    }

    @Test
    void findByGroupDelegatesAndPropagatesFailures() throws CardDomainException {
        when(reasonRepository.findByGroupId(ReasonGroup.BLOCK_CARD))
                .thenReturn(List.of(reason(1L))).thenThrow(new IllegalStateException("boom"));

        assertThat(reasonService().findByGroupId(ReasonGroup.BLOCK_CARD)).hasSize(1);
        assertThatThrownBy(() -> reasonService().findByGroupId(ReasonGroup.BLOCK_CARD))
                .isInstanceOf(IllegalStateException.class);
    }

    // ---------- status ----------

    @Test
    void findStatusByCodeDelegates() throws CardDomainException {
        Status existing = status(1L, "C");
        when(statusRepository.findByCode("C")).thenReturn(existing);

        assertThat(statusService().findByCode("C")).isSameAs(existing);
    }

    @Test
    void createStatusSavesNewStatus() throws CardDomainException {
        Status input = status(null, "C");
        Status saved = status(9L, "C");
        when(statusRepository.findByCode("C")).thenReturn(null);
        when(statusRepository.save(input)).thenReturn(saved);

        assertThat(statusService().createStatus(input)).isSameAs(saved);
    }

    @Test
    void createStatusTreatsExistingWithoutIdAsNew() throws CardDomainException {
        Status input = status(null, "C");
        Status saved = status(9L, "C");
        when(statusRepository.findByCode("C")).thenReturn(status(null, "C"));
        when(statusRepository.save(input)).thenReturn(saved);

        assertThat(statusService().createStatus(input)).isSameAs(saved);
    }

    @Test
    void createStatusRequiresStatusAndCode() {
        assertThatThrownBy(() -> statusService().createStatus(null))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> statusService().createStatus(status(null, null)))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
    }

    @Test
    void createStatusRejectsExistingCode() throws CardDomainException {
        when(statusRepository.findByCode("C")).thenReturn(status(1L, "C"));

        assertThatThrownBy(() -> statusService().createStatus(status(null, "C")))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.BAD_REQUEST));
    }

    @Test
    void createStatusFailsWhenSaveReturnsNothingUsable() throws CardDomainException {
        Status input = status(null, "C");
        when(statusRepository.findByCode("C")).thenReturn(null);
        when(statusRepository.save(input)).thenReturn(null).thenReturn(status(null, "C"));

        assertThatThrownBy(() -> statusService().createStatus(input))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> statusService().createStatus(input))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void updatePersianDescriptionChangesExistingStatus() throws CardDomainException {
        Status existing = status(1L, "C");
        when(statusRepository.findByCode("C")).thenReturn(existing);
        when(statusRepository.save(existing)).thenReturn(existing);

        Status result = statusService().updateStatusPersianDescription(new Status("جدید", "C", null));

        assertThat(result.getPersianDescription()).isEqualTo("جدید");
    }

    @Test
    void updatePersianDescriptionValidatesInput() {
        assertThatThrownBy(() -> statusService().updateStatusPersianDescription(null))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> statusService().updateStatusPersianDescription(new Status("x", null, null)))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> statusService().updateStatusPersianDescription(new Status(null, "C", null)))
                .satisfies(t -> assertDomainError(t, StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST));
    }

    @Test
    void updatePersianDescriptionOfUnknownStatusIsNotFound() throws CardDomainException {
        when(statusRepository.findByCode("C")).thenReturn(null).thenReturn(status(null, "C"));

        assertThatThrownBy(() -> statusService().updateStatusPersianDescription(new Status("x", "C", null)))
                .satisfies(t -> assertDomainError(t, StatusService.ID_NOT_FOUND, HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> statusService().updateStatusPersianDescription(new Status("x", "C", null)))
                .satisfies(t -> assertDomainError(t, StatusService.ID_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Test
    void updatePersianDescriptionFailsWhenSaveReturnsNothingUsable() throws CardDomainException {
        Status existing = status(1L, "C");
        when(statusRepository.findByCode("C")).thenReturn(existing);
        when(statusRepository.save(existing)).thenReturn(null).thenReturn(status(null, "C"));

        assertThatThrownBy(() -> statusService().updateStatusPersianDescription(new Status("x", "C", null)))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> statusService().updateStatusPersianDescription(new Status("x", "C", null)))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void listStatusesDelegatesAndWrapsFailures() throws CardDomainException {
        Map<String, String> params = Map.of();
        Page<Status> page = new PageImpl<>(List.of(status(1L, "C")));
        when(statusRepository.findAllStatuses(params)).thenReturn(page).thenThrow(new IllegalStateException("db"));

        assertThat(statusService().listStatuses(params, "me", "ip")).isSameAs(page);
        assertThatThrownBy(() -> statusService().listStatuses(params, "me", "ip"))
                .satisfies(t -> assertDomainError(t, StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
