package com.sample.system.card.service.domain.handler;

import com.sample.system.card.service.domain.command.CreateStatusCommand;
import com.sample.system.card.service.domain.command.UpdateStatusCommand;
import com.sample.system.card.service.domain.command.feeProfile.*;
import com.sample.system.card.service.domain.command.reason.CreateReasonCommand;
import com.sample.system.card.service.domain.command.reason.GetReasonQuery;
import com.sample.system.card.service.domain.entity.FeeProfile;
import com.sample.system.card.service.domain.entity.Reason;
import com.sample.system.card.service.domain.entity.Status;
import com.sample.system.card.service.domain.enums.ReasonGroup;
import com.sample.system.card.service.domain.event.feeProfile.FeeProfileCreatedEvent;
import com.sample.system.card.service.domain.event.reason.ReasonCreatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.FeeProfileCommandHandler;
import com.sample.system.card.service.domain.handler.Command.ReasonCommandHandler;
import com.sample.system.card.service.domain.handler.Command.StatusCommandHandler;
import com.sample.system.card.service.domain.handler.query.FeeProfileQueryHandler;
import com.sample.system.card.service.domain.handler.query.ReasonQueryHandler;
import com.sample.system.card.service.domain.handler.query.StatusQueryHandler;
import com.sample.system.card.service.domain.mapper.FeeProfileDataMapper;
import com.sample.system.card.service.domain.mapper.ReasonDataMapper;
import com.sample.system.card.service.domain.mapper.StatusDataMapper;
import com.sample.system.card.service.domain.ports.input.service.FeeProfileService;
import com.sample.system.card.service.domain.ports.input.service.ReasonService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.valueObject.FeeProfileId;
import com.sample.system.card.service.domain.valueObject.ReasonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeReasonStatusHandlersTest {

    @Mock
    private FeeProfileService feeService;
    @Mock
    private ReasonService reasonService;
    @Mock
    private StatusService statusService;

    private FeeProfileCommandHandler feeCommands;
    private FeeProfileQueryHandler feeQueries;
    private ReasonCommandHandler reasonCommands;
    private ReasonQueryHandler reasonQueries;
    private StatusCommandHandler statusCommands;
    private StatusQueryHandler statusQueries;

    @BeforeEach
    void setUp() {
        FeeProfileDataMapper feeMapper = new FeeProfileDataMapper();
        ReasonDataMapper reasonMapper = new ReasonDataMapper();
        StatusDataMapper statusMapper = new StatusDataMapper();
        feeCommands = new FeeProfileCommandHandler(feeService, feeMapper);
        feeQueries = new FeeProfileQueryHandler(feeService, feeMapper);
        reasonCommands = new ReasonCommandHandler(reasonService, reasonMapper);
        reasonQueries = new ReasonQueryHandler(reasonService, reasonMapper);
        statusCommands = new StatusCommandHandler(statusService, statusMapper);
        statusQueries = new StatusQueryHandler(statusMapper, statusService);
    }

    private static FeeProfile profile(long id) {
        FeeProfile profile = new FeeProfile();
        profile.setId(new FeeProfileId(id));
        profile.setName("Basic");
        profile.setIsActive(true);
        return profile;
    }

    private static Reason reason(long id) {
        Reason reason = Reason.builder().code(1).title("t").reason("r").groupId(ReasonGroup.BLOCK_CARD).build();
        reason.setId(new ReasonId(id));
        return reason;
    }

    private static Status status(long id) {
        Status status = new Status("فارسی", "C", "d");
        status.setId(id);
        return status;
    }

    // ---------- fee profile ----------

    @Test
    void createFeeProfileBuildsResponse() throws CardDomainException {
        CreateFeeProfileCommand command = new CreateFeeProfileCommand("Basic", "d", true);
        when(feeService.createFeeProfile(command)).thenReturn(new FeeProfileCreatedEvent(profile(1L), ZonedDateTime.now()));

        assertThat(feeCommands.createFeeProfile(command).getMessage()).isEqualTo("FeeProfile created successfully");
    }

    @Test
    void feeProfileQueriesMapResults() throws CardDomainException {
        GetFeeProfileQuery query = new GetFeeProfileQuery(2L);
        GetAllFeeProfilesQuery all = new GetAllFeeProfilesQuery();
        Map<String, String> params = Map.of();
        when(feeService.getFeeProfile(query)).thenReturn(profile(2L));
        when(feeService.getAllFeeProfiles(all)).thenReturn(List.of(profile(1L), profile(2L)));
        when(feeService.listFeeProfiles(params, "me", "ip")).thenReturn(new PageImpl<>(List.of(profile(1L))));

        assertThat(feeQueries.getFeeProfile(query).id()).isEqualTo(2L);
        assertThat(feeQueries.getAllFeeProfiles(all)).hasSize(2);
        assertThat(feeQueries.listFeeProfiles(params, "me", "ip").getList()).hasSize(1);
    }

    @Test
    void listFeeProfilesWithoutPageGivesNull() throws CardDomainException {
        Map<String, String> params = Map.of();
        when(feeService.listFeeProfiles(params, "me", "ip")).thenReturn(null);

        assertThat(feeQueries.listFeeProfiles(params, "me", "ip")).isNull();
    }

    // ---------- reason ----------

    @Test
    void createReasonBuildsResponse() throws CardDomainException {
        CreateReasonCommand command = new CreateReasonCommand(1, "t", "r", ReasonGroup.BLOCK_CARD);
        when(reasonService.createReason(command)).thenReturn(new ReasonCreatedEvent(reason(1L), ZonedDateTime.now()));

        assertThat(reasonCommands.createReason(command).getMessage()).isEqualTo("Reason created successfully");
    }

    @Test
    void reasonQueriesMapResults() throws CardDomainException {
        GetReasonQuery query = new GetReasonQuery(3L);
        Map<String, String> params = Map.of();
        when(reasonService.getReason(query)).thenReturn(reason(3L));
        when(reasonService.getAllReasons(params, "me", "ip")).thenReturn(new PageImpl<>(List.of(reason(1L))));
        when(reasonService.findByGroupId(ReasonGroup.BLOCK_CARD)).thenReturn(List.of(reason(1L), reason(2L)));

        assertThat(reasonQueries.getReason(query).id()).isEqualTo(3L);
        assertThat(reasonQueries.getAllReasons(params, "me", "ip").getList()).hasSize(1);
        assertThat(reasonQueries.findByGroup(ReasonGroup.BLOCK_CARD).getList()).hasSize(2);
    }

    @Test
    void reasonQueriesWithoutResultGiveNull() throws CardDomainException {
        Map<String, String> params = Map.of();
        when(reasonService.getAllReasons(params, "me", "ip")).thenReturn(null);

        assertThat(reasonQueries.getAllReasons(params, "me", "ip")).isNull();
    }

    @Test
    void findByGroupWithNoReasonsGivesEmptyList() throws CardDomainException {
        when(reasonService.findByGroupId(ReasonGroup.ACTIVE_CARD)).thenReturn(List.of());

        assertThat(reasonQueries.findByGroup(ReasonGroup.ACTIVE_CARD).getList()).isEmpty();
    }

    // ---------- status ----------

    @Test
    void createStatusMapsCommandAndResult() throws CardDomainException {
        when(statusService.createStatus(org.mockito.ArgumentMatchers.any(Status.class))).thenReturn(status(5L));

        var response = statusCommands.createStatus(new CreateStatusCommand("C", "d", "فارسی"));

        assertThat(response.getStatusId()).isEqualTo(5L);
        assertThat(response.getMessage()).isEqualTo("Status created successfully");
    }

    @Test
    void updateStatusMapsCommandAndResult() throws CardDomainException {
        when(statusService.updateStatusPersianDescription(org.mockito.ArgumentMatchers.any(Status.class))).thenReturn(status(6L));

        var response = statusCommands.updateStatusPersianDescription(new UpdateStatusCommand("C", "جدید"));

        assertThat(response.getStatusId()).isEqualTo(6L);
        assertThat(response.getMessage()).isEqualTo("Status persianDescription updated successfully");
    }

    @Test
    void listStatusesMapsPageOrNull() throws CardDomainException {
        Map<String, String> params = Map.of();
        when(statusService.listStatuses(params, "me", "ip"))
                .thenReturn(new PageImpl<>(List.of(status(1L)))).thenReturn(null);

        assertThat(statusQueries.listStatuses(params, "me", "ip").getList()).hasSize(1);
        assertThat(statusQueries.listStatuses(params, "me", "ip")).isNull();
    }
}
