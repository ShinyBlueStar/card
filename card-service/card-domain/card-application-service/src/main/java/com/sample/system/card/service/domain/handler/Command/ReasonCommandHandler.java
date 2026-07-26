package com.sample.system.card.service.domain.handler.Command;

import com.sample.system.card.service.domain.command.reason.CreateReasonCommand;
import com.sample.system.card.service.domain.event.reason.ReasonCreatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.ReasonDataMapper;
import com.sample.system.card.service.domain.ports.input.service.ReasonService;
import com.sample.system.card.service.domain.response.reason.CreateReasonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReasonCommandHandler {

    private final ReasonService reasonService;
    private final ReasonDataMapper reasonDataMapper;

    @Transactional
    public CreateReasonResponse createReason(CreateReasonCommand command) throws CardDomainException {
        log.info("Starting Reason creation handler - Title: {}", command.getTitle());
        ReasonCreatedEvent event = reasonService.createReason(command);
        log.debug("Mapping Reason to CreateReasonResponse");
        return reasonDataMapper.reasonToCreateResponse(event.getReason(), "Reason created successfully");
    }
}

