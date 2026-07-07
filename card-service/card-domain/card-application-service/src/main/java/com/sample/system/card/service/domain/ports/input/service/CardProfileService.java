package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.command.cardProfile.GetAllCardProfilesQuery;
import com.sample.system.card.service.domain.command.cardProfile.GetCardProfileQuery;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.event.cardProfile.CardProfileActivatedEvent;
import com.sample.system.card.service.domain.event.cardProfile.CardProfileCreatedEvent;
import com.sample.system.card.service.domain.event.cardProfile.CardProfileDeactivatedEvent;
import com.sample.system.card.service.domain.event.cardProfile.CardProfileUpdatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface CardProfileService extends BaseService {

    CardProfileCreatedEvent createCardProfile(CardProfile cardProfile)
            throws CardDomainException;

    CardProfileUpdatedEvent updateCardProfile(CardProfile cardProfile)
            throws CardDomainException;

    CardProfileActivatedEvent activateCardProfile(CardProfile cardProfile)
            throws CardDomainException;

    CardProfileDeactivatedEvent deactivateCardProfile(CardProfile cardProfile)
            throws CardDomainException;

    CardProfile getCardProfile(GetCardProfileQuery query)
            throws CardDomainException;

    List<CardProfile> getAllCardProfiles(GetAllCardProfilesQuery query)
            throws CardDomainException;

    Page<CardProfile> listCardProfiles(Map<String, String> map, String caller, String ip) throws CardDomainException;

    CardProfile findById(Long id) throws CardDomainException;
}