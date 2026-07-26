package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.entity.Status;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;

    @Override
    public Status findByCode(String code) throws CardDomainException{
        return statusRepository.findByCode(code);
    }

    @Override
    @Transactional
    public Status createStatus(Status status) throws CardDomainException {
        if (status == null || status.getCode() == null) {
            throw new CardDomainException("Status code is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }

        Status existingStatus = statusRepository.findByCode(status.getCode());
        if (existingStatus != null && existingStatus.getId() != null) {
            throw new CardDomainException(
                    "Status with code " + status.getCode() + " already exists",
                    StatusService.GENERAL_ERROR,
                    HttpStatus.BAD_REQUEST);
        }

        Status savedStatus = statusRepository.save(status);
        if (savedStatus == null || savedStatus.getId() == null) {
            throw new CardDomainException(
                    "Could not save status with code " + status.getCode(),
                    StatusService.GENERAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return savedStatus;
    }

    @Override
    @Transactional
    public Status updateStatusPersianDescription(Status status) throws CardDomainException {
        if (status == null || status.getCode() == null) {
            throw new CardDomainException("Status code is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        if (status.getPersianDescription() == null) {
            throw new CardDomainException("Persian description is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }

        Status existingStatus = statusRepository.findByCode(status.getCode());
        if (existingStatus == null || existingStatus.getId() == null) {
            throw new CardDomainException("Status not found with code: " + status.getCode(),
                    StatusService.ID_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        existingStatus.setPersianDescription(status.getPersianDescription());
        Status savedStatus = statusRepository.save(existingStatus);
        if (savedStatus == null || savedStatus.getId() == null) {
            throw new CardDomainException(
                    "Could not update status with code " + status.getCode(),
                    StatusService.GENERAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return savedStatus;
    }

    @Override
    public Page<Status> listStatuses(Map<String, String> params, String caller, String ip) throws CardDomainException {
        log.info("Starting status list retrieval service");
        try {
            Page<Status> allStatuses = statusRepository.findAllStatuses(params);
            return allStatuses;
        } catch (Exception e) {
            log.error("Error occurred in status list retrieval service - error: {}", e.getMessage(), e);
            throw new CardDomainException("", StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
