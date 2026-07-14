package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.command.feeProfile.*;
import com.sample.system.card.service.domain.entity.FeeProfile;
import com.sample.system.card.service.domain.event.feeProfile.FeeProfileCreatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface FeeProfileService extends BaseService {

    FeeProfileCreatedEvent createFeeProfile(@Valid CreateFeeProfileCommand command) throws CardDomainException;

    FeeProfile getFeeProfile(@Valid GetFeeProfileQuery query) throws CardDomainException;

    List<FeeProfile> getAllFeeProfiles(@Valid GetAllFeeProfilesQuery query) throws CardDomainException;

    Page<FeeProfile> listFeeProfiles(Map<String, String> params, String caller, String ip) throws CardDomainException;
}

