package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.feeProfile.*;
import com.sample.system.card.service.domain.entity.FeeProfile;
import com.sample.system.card.service.domain.event.feeProfile.FeeProfileCreatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.FeeProfileDataMapper;
import com.sample.system.card.service.domain.ports.input.service.FeeProfileService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.FeeProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class FeeProfileServiceImpl implements FeeProfileService {

    private final FeeProfileRepository repository;
    private final FeeProfileDataMapper feeProfileDataMapper;

    @Override
    @Transactional
    public FeeProfileCreatedEvent createFeeProfile(CreateFeeProfileCommand command) throws CardDomainException {
        log.info("Starting FeeProfile creation service - Name: {}", command.getName());
        if (repository.existsByName(command.getName())) {
            throw new CardDomainException("FeeProfile with name " + command.getName() + " already exists",
                    StatusService.GENERAL_ERROR, HttpStatus.BAD_REQUEST);
        }
        FeeProfile feeProfile = feeProfileDataMapper.createCommandToFeeProfile(command);
        FeeProfile savedFeeProfile = repository.save(feeProfile);
        if (savedFeeProfile == null) {
            log.error("Could not save FeeProfile with name: {}", command.getName());
            throw new CardDomainException("Could not save FeeProfile with name " + command.getName());
        }
        FeeProfileCreatedEvent event = new FeeProfileCreatedEvent(savedFeeProfile, ZonedDateTime.now());
        log.info("FeeProfile creation process completed successfully - ProfileId: {}, Name: {}",
                savedFeeProfile.getId().getValue(), savedFeeProfile.getName());
        return event;
    }

    @Override
    public FeeProfile getFeeProfile(GetFeeProfileQuery query) throws CardDomainException {
        log.info("Getting fee profile with id: {}", query.getFeeProfileId());
        return repository.findById(query.getFeeProfileId())
                .orElseThrow(() -> new CardDomainException("FeeProfile not found with id: " + query.getFeeProfileId(),
                        StatusService.FEE_PROFILE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeeProfile> getAllFeeProfiles(GetAllFeeProfilesQuery query) throws CardDomainException {
        log.info("Getting all fee profiles with activeOnly: {}", query.getActiveOnly());
        List<FeeProfile> feeProfiles = query.getActiveOnly() != null && query.getActiveOnly()
                ? repository.findActiveFeeProfiles()
                : repository.findAll();
        log.info("Found {} fee profiles", feeProfiles.size());
        return feeProfiles;
    }

    @Override
    public Page<FeeProfile> listFeeProfiles(Map<String, String> params, String caller, String ip) throws CardDomainException {
        log.info("Starting FeeProfiles list service");
        Page<FeeProfile> allProfiles = repository.findAllProfiles(params);
        return allProfiles;
    }
}

