package com.sample.system.card.service.domain.handler.Command;

import com.sample.system.card.service.domain.command.feeProfile.CreateFeeProfileCommand;
import com.sample.system.card.service.domain.event.feeProfile.FeeProfileCreatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.FeeProfileDataMapper;
import com.sample.system.card.service.domain.ports.input.service.FeeProfileService;
import com.sample.system.card.service.domain.response.feeProfile.CreateFeeProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeeProfileCommandHandler {

    private final FeeProfileService feeProfileService;
    private final FeeProfileDataMapper feeProfileDataMapper;

    @Transactional
    public CreateFeeProfileResponse createFeeProfile(CreateFeeProfileCommand command) throws CardDomainException {
        log.info("Starting FeeProfile creation handler - Name: {}", command.getName());
        FeeProfileCreatedEvent event = feeProfileService.createFeeProfile(command);
        log.debug("Mapping FeeProfile to CreateFeeProfileResponse");
        return feeProfileDataMapper.feeProfileToCreateResponse(event.getFeeProfile(), "FeeProfile created successfully");
    }
}

