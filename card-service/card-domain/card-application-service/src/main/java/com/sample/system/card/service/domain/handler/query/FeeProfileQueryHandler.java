package com.sample.system.card.service.domain.handler.query;

import com.sample.system.card.service.domain.command.feeProfile.*;
import com.sample.system.card.service.domain.entity.FeeProfile;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.FeeProfileDataMapper;
import com.sample.system.card.service.domain.ports.input.service.FeeProfileService;
import com.sample.system.card.service.domain.response.feeProfile.FeeProfileListResponse;
import com.sample.system.card.service.domain.response.feeProfile.GetFeeProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeeProfileQueryHandler {

    private final FeeProfileService feeProfileService;
    private final FeeProfileDataMapper feeProfileDataMapper;

    @Transactional(readOnly = true)
    public GetFeeProfileResponse getFeeProfile(GetFeeProfileQuery query) throws CardDomainException {
        log.info("Getting fee profile with id: {}", query.getFeeProfileId());
        FeeProfile feeProfile = feeProfileService.getFeeProfile(query);
        log.info("FeeProfile retrieval completed by QueryHandler - ProfileId: {}", query.getFeeProfileId());
        return feeProfileDataMapper.feeProfileToGetResponse(feeProfile);
    }

    public List<GetFeeProfileResponse> getAllFeeProfiles(GetAllFeeProfilesQuery query) throws CardDomainException {
        log.info("Getting all fee profiles with activeOnly: {}", query.getActiveOnly());
        List<FeeProfile> feeProfiles = feeProfileService.getAllFeeProfiles(query);
        log.info("Found {} fee profiles", feeProfiles.size());
        return feeProfileDataMapper.feeProfilesToGetResponses(feeProfiles);
    }

    public FeeProfileListResponse listFeeProfiles(Map<String, String> map, String caller, String ip) throws CardDomainException {
        org.springframework.data.domain.Page<FeeProfile> responses = feeProfileService.listFeeProfiles(map, caller, ip);
        log.debug("FeeProfiles retrieval completed by QueryHandler - Count: {}",
                responses != null ? responses.getContent().size() : 0);
        return feeProfileDataMapper.feeProfilesToGetListResponse(responses);
    }
}

