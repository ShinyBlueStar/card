package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.command.reason.*;
import com.sample.system.card.service.domain.entity.Reason;
import com.sample.system.card.service.domain.enums.ReasonGroup;
import com.sample.system.card.service.domain.event.reason.ReasonCreatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ReasonService extends BaseService {
    Reason findById(Long reasonId) throws CardDomainException;

    ReasonCreatedEvent createReason(@Valid CreateReasonCommand command) throws CardDomainException;

    Reason getReason(@Valid GetReasonQuery query) throws CardDomainException;

    Page<Reason> getAllReasons(Map<String, String> map, String caller, String ip)
            throws CardDomainException;

    List<Reason> findByGroupId(ReasonGroup groupId) throws CardDomainException;
}
