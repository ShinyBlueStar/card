package com.sample.system.card.service.domain.handler.query;

import com.sample.system.card.service.domain.command.cardProfile.GetCardProfileQuery;
import com.sample.system.card.service.domain.command.cardProfile.GetAllCardProfilesQuery;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardProfileDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardProfileService;
import com.sample.system.card.service.domain.response.profile.CardProfileListResponse;
import com.sample.system.card.service.domain.response.profile.GetCardProfileResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class CardProfileQueryHandler {

    private final CardProfileDataMapper cardProfileDataMapper;
    private final CardProfileService cardProfileService;

    public GetCardProfileResponse getCardProfile(GetCardProfileQuery query) throws CardDomainException{
        log.info("Getting card profile with id: {}", query.getCardProfileId());
        CardProfile cardProfile = cardProfileService.getCardProfile(query);
        log.info("CardProfile retrieval completed by QueryHandler - ProfileId: {}", query.getCardProfileId());
        return cardProfileDataMapper.profileToGetResponse(cardProfile);
    }

    public List<GetCardProfileResponse> getAllCardProfiles(GetAllCardProfilesQuery query) throws CardDomainException{
        log.info("Getting all card profiles with activeOnly: {}", query.getActiveOnly());
        List<CardProfile> cardProfiles = cardProfileService.getAllCardProfiles(query);
        log.info("Found {} card profiles", cardProfiles.size());
        return cardProfileDataMapper.profilesToGetResponses(cardProfiles);
    }

    public CardProfileListResponse listCardProfiles(Map<String, String> map, String caller, String ip) throws CardDomainException{
        Page<CardProfile> responses = cardProfileService.listCardProfiles(map, caller, ip);
        log.debug("CardProfiles retrieval completed by QueryHandler - Count: {}",
                responses != null ? responses.getContent().size() : 0);
        return cardProfileDataMapper.profilesToGetListResponse(responses);
    }
}