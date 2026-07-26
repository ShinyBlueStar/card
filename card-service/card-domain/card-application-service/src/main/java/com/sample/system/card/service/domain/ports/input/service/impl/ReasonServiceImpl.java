package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.reason.*;
import com.sample.system.card.service.domain.entity.Reason;
import com.sample.system.card.service.domain.enums.ReasonGroup;
import com.sample.system.card.service.domain.event.reason.ReasonCreatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.ReasonDataMapper;
import com.sample.system.card.service.domain.ports.input.service.ReasonService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.ReasonRepository;
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
class ReasonServiceImpl implements ReasonService {
    private final ReasonRepository repository;
    private final ReasonDataMapper reasonDataMapper;

    @Override
    @Transactional(readOnly = true)
    public Reason findById(Long reasonId) throws CardDomainException {
        return repository.findById(reasonId).orElseThrow(() ->
                new CardDomainException("Reason with id " + reasonId + " not found",
                StatusService.REASON_NOT_FOUND, HttpStatus.BAD_REQUEST));
    }

    @Override
    @Transactional
    public ReasonCreatedEvent createReason(CreateReasonCommand command) throws CardDomainException {
        log.info("Starting Reason creation service - Title: {}", command.getTitle());
        try {
            if (repository.existsByCode(command.getCode())) {
                throw new CardDomainException("Reason with code " + command.getCode() + " already exists",
                        StatusService.GENERAL_ERROR, HttpStatus.BAD_REQUEST);
            }
            Reason reason = reasonDataMapper.createCommandToReason(command);
            Reason savedReason = repository.save(reason);
            if (savedReason == null) {
                log.error("Could not save Reason with code: {}", command.getCode());
                throw new CardDomainException("Could not save Reason with code " + command.getCode());
            }
            ReasonCreatedEvent event = new ReasonCreatedEvent(savedReason, ZonedDateTime.now());
            log.info("Reason creation process completed successfully - ReasonId: {}, Code: {}",
                    savedReason.getId().getValue(), savedReason.getCode());
            return event;
        } catch (Exception e) {
            log.error("Error occurred during Reason creation - Code: {}, Error: {}",
                    command.getCode(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Reason getReason(GetReasonQuery query) throws CardDomainException {
        log.info("Getting reason with id: {}", query.getReasonId());
        return repository.findById(query.getReasonId())
                .orElseThrow(() -> new CardDomainException("Reason not found with id: " + query.getReasonId(),
                        StatusService.REASON_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Override
    public Page<Reason> getAllReasons(Map<String, String> map, String caller, String ip) throws CardDomainException {
        log.info("Getting all reasons");
        try {
            Page<Reason> reasons = repository.findAll(map);
            log.info("Found {} reasons", reasons.getContent().size());
            return reasons;
        } catch (Exception e) {
            log.error("Error occurred during Reasons retrieval - Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reason> findByGroupId(ReasonGroup groupId) throws CardDomainException {
        log.info("Finding reasons by groupId: {}", groupId);
        try {
            List<Reason> reasons = repository.findByGroupId(groupId);
            log.info("Found {} reasons for groupId: {}", reasons.size(), groupId);
            return reasons;
        } catch (Exception e) {
            log.error("Error occurred during findByGroupId - groupId: {}, Error: {}", groupId, e.getMessage(), e);
            throw e;
        }
    }
}
