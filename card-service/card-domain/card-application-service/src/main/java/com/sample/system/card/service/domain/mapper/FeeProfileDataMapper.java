package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.feeProfile.CreateFeeProfileCommand;
import com.sample.system.card.service.domain.entity.Fee;
import com.sample.system.card.service.domain.entity.FeeProfile;
import com.sample.system.card.service.domain.response.feeProfile.CreateFeeProfileResponse;
import com.sample.system.card.service.domain.response.feeProfile.FeeProfileListResponse;
import com.sample.system.card.service.domain.response.feeProfile.GetFeeProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Application-layer mapper utilities for FeeProfile domain aggregate.
 * Manual mapping to stay consistent with removal of MapStruct from DataMappers.
 */
@Component
public class FeeProfileDataMapper {

    public FeeProfile create(String name) {
        FeeProfile profile = new FeeProfile();
        profile.setName(name);
        profile.setFees(new ArrayList<>());
        return profile;
    }

    public FeeProfile createCommandToFeeProfile(CreateFeeProfileCommand command) {
        FeeProfile profile = new FeeProfile();
        profile.setName(command.getName());
        profile.setIsActive(command.getIsActive() != null ? command.getIsActive() : Boolean.TRUE);
        profile.setFees(new ArrayList<>());
        return profile;
    }

    public void update(FeeProfile profile, String name) {
        if (name != null) {
            profile.setName(name);
        }
    }

    public List<Fee> fees(FeeProfile profile) {
        return profile.getFees() != null ? profile.getFees() : List.of();
    }

    public CreateFeeProfileResponse feeProfileToCreateResponse(FeeProfile feeProfile, String message) {
        return CreateFeeProfileResponse.builder()
                .feeProfileId(feeProfile.getId() != null ? feeProfile.getId().getValue() : null)
                .name(feeProfile.getName())
                .description(null)
                .isActive(feeProfile.getIsActive())
                .message(message)
                .build();
    }

    public GetFeeProfileResponse feeProfileToGetResponse(FeeProfile feeProfile) {
        return new GetFeeProfileResponse(
                feeProfile.getId() != null ? feeProfile.getId().getValue() : null,
                feeProfile.getName(),
                null,
                feeProfile.getIsActive());
    }

    public List<GetFeeProfileResponse> feeProfilesToGetResponses(List<FeeProfile> feeProfiles) {
        return feeProfiles.stream()
                .map(this::feeProfileToGetResponse)
                .toList();
    }

    public FeeProfileListResponse feeProfilesToGetListResponse(Page<FeeProfile> feeProfiles) {
        if (feeProfiles == null) return null;

        FeeProfileListResponse feeProfileListResponse = new FeeProfileListResponse();
        feeProfileListResponse.setList(feeProfiles.getContent().stream()
                .map(this::feeProfileToGetResponse).toList());
        feeProfileListResponse.setNumber(feeProfiles.getNumber());
        feeProfileListResponse.setSize(feeProfiles.getSize());
        feeProfileListResponse.setTotalElements(feeProfiles.getTotalElements());
        feeProfileListResponse.setTotalPages(feeProfiles.getTotalPages());
        return feeProfileListResponse;
    }
}
