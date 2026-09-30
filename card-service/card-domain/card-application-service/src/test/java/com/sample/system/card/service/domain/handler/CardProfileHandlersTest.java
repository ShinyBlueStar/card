package com.sample.system.card.service.domain.handler;

import com.sample.system.card.service.domain.command.cardProfile.*;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.event.cardProfile.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardProfileCommandHandler;
import com.sample.system.card.service.domain.handler.query.CardProfileQueryHandler;
import com.sample.system.card.service.domain.mapper.CardNumberPatternDataMapper;
import com.sample.system.card.service.domain.mapper.CardProfileDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardProfileService;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardProfileHandlersTest {

    @Mock
    private CardProfileService service;

    private CardProfileCommandHandler commands;
    private CardProfileQueryHandler queries;

    @BeforeEach
    void setUp() {
        CardProfileDataMapper mapper = new CardProfileDataMapper(new CardNumberPatternDataMapper());
        commands = new CardProfileCommandHandler(service, mapper);
        queries = new CardProfileQueryHandler(mapper, service);
    }

    private static CardProfile profile(long id, boolean active) {
        CardProfile profile = CardProfile.builder().profileName("Gold").isActive(active).build();
        profile.setId(new CardProfileId(id));
        return profile;
    }

    @Test
    void createMapsCommandAndReturnsSuccess() throws CardDomainException {
        when(service.createCardProfile(any(CardProfile.class))).thenReturn(new CardProfileCreatedEvent(profile(1L, true)));

        var response = commands.createCardProfile(CreateCardProfileCommand.builder().profileName("Gold").build());

        assertThat(response.cardProfileId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("success");
    }

    @Test
    void updateMapsCommandAndReturnsMessage() throws CardDomainException {
        when(service.updateCardProfile(any(CardProfile.class))).thenReturn(new CardProfileUpdatedEvent(profile(2L, true)));

        var response = commands.updateCardProfile(UpdateCardProfileCommand.builder().cardProfileId(2L).build());

        assertThat(response.cardProfileId()).isEqualTo(2L);
        assertThat(response.message()).isEqualTo("Card Profile updated successfully!");
    }

    @Test
    void activateAndDeactivateMapCommands() throws CardDomainException {
        when(service.activateCardProfile(any(CardProfile.class))).thenReturn(new CardProfileActivatedEvent(profile(3L, true)));
        when(service.deactivateCardProfile(any(CardProfile.class))).thenReturn(new CardProfileDeactivatedEvent(profile(4L, false)));

        assertThat(commands.activateCardProfile(ActivateCardProfileCommand.builder().cardProfileId(3L).build()).message())
                .isEqualTo("success");
        assertThat(commands.deactivateCardProfile(DeactivateCardProfileCommand.builder().cardProfileId(4L).build()).message())
                .isEqualTo("Success");
    }

    @Test
    void changeStatusToActiveActivatesTheProfile() throws CardDomainException {
        when(service.activateCardProfile(any(CardProfile.class))).thenReturn(new CardProfileActivatedEvent(profile(5L, true)));

        var response = commands.changeStatus(new ChangeCardProfileStatusCommand(5L, true));

        ArgumentCaptor<CardProfile> captor = ArgumentCaptor.forClass(CardProfile.class);
        verify(service).activateCardProfile(captor.capture());
        assertThat(captor.getValue().getId().getValue()).isEqualTo(5L);
        assertThat(response.message()).isEqualTo("Card profile activated successfully!");
    }

    @Test
    void changeStatusToInactiveOrMissingDeactivatesTheProfile() throws CardDomainException {
        when(service.deactivateCardProfile(any(CardProfile.class))).thenReturn(new CardProfileDeactivatedEvent(profile(6L, false)));

        assertThat(commands.changeStatus(new ChangeCardProfileStatusCommand(6L, false)).message())
                .isEqualTo("Card profile deactivated successfully!");
        assertThat(commands.changeStatus(new ChangeCardProfileStatusCommand(6L, null)).isActive()).isFalse();
    }

    @Test
    void queriesMapServiceResults() throws CardDomainException {
        GetCardProfileQuery query = new GetCardProfileQuery(7L);
        GetAllCardProfilesQuery all = new GetAllCardProfilesQuery();
        Map<String, String> params = Map.of();
        when(service.getCardProfile(query)).thenReturn(profile(7L, true));
        when(service.getAllCardProfiles(all)).thenReturn(List.of(profile(1L, true), profile(2L, true)));
        when(service.listCardProfiles(params, "me", "ip")).thenReturn(new PageImpl<>(List.of(profile(1L, true))));

        assertThat(queries.getCardProfile(query).id()).isEqualTo(7L);
        assertThat(queries.getAllCardProfiles(all)).hasSize(2);
        assertThat(queries.listCardProfiles(params, "me", "ip").getList()).hasSize(1);
    }

    @Test
    void listWithoutPageGivesNull() throws CardDomainException {
        Map<String, String> params = Map.of();
        when(service.listCardProfiles(params, "me", "ip")).thenReturn(null);

        assertThat(queries.listCardProfiles(params, "me", "ip")).isNull();
    }
}
