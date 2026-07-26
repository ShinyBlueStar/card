package com.sample.system.card.service.domain.handler.Command;

import com.sample.system.card.service.domain.command.CreateStatusCommand;
import com.sample.system.card.service.domain.command.UpdateStatusCommand;
import com.sample.system.card.service.domain.entity.Status;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.StatusDataMapper;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.response.CreateStatusResponse;
import com.sample.system.card.service.domain.response.UpdateStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusCommandHandler {

    private final StatusService statusService;
    private final StatusDataMapper statusDataMapper;

    @Transactional
    public CreateStatusResponse createStatus(CreateStatusCommand command) throws CardDomainException {
        log.info("Creating status with code: {}", command.getCode());
        Status status = statusDataMapper.createCommandToStatus(command);
        Status createdStatus = statusService.createStatus(status);
        return statusDataMapper.statusToCreateResponse(createdStatus, "Status created successfully");
    }

    @Transactional
    public UpdateStatusResponse updateStatusPersianDescription(UpdateStatusCommand command) throws CardDomainException {
        log.info("Updating status persianDescription for code: {}", command.getCode());
        Status status = statusDataMapper.updateCommandToStatus(command);
        Status updatedStatus = statusService.updateStatusPersianDescription(status);
        return statusDataMapper.statusToUpdateResponse(updatedStatus, "Status persianDescription updated successfully");
    }
}
