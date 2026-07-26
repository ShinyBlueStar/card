package com.sample.system.card.service.domain.handler.query;

import com.sample.system.card.service.domain.command.reason.*;
import com.sample.system.card.service.domain.entity.Reason;
import com.sample.system.card.service.domain.enums.ReasonGroup;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.ReasonDataMapper;
import com.sample.system.card.service.domain.ports.input.service.ReasonService;
import com.sample.system.card.service.domain.response.reason.GetReasonResponse;
import com.sample.system.card.service.domain.response.reason.ReasonListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReasonQueryHandler {

    private final ReasonService reasonService;
    private final ReasonDataMapper reasonDataMapper;

    @Transactional(readOnly = true)
    public GetReasonResponse getReason(GetReasonQuery query) throws CardDomainException {
        log.info("Getting reason with id: {}", query.getReasonId());
        Reason reason = reasonService.getReason(query);
        log.info("Reason retrieval completed by QueryHandler - ReasonId: {}", query.getReasonId());
        return reasonDataMapper.reasonToGetResponse(reason);
    }

    public ReasonListResponse getAllReasons(Map<String, String> map, String caller, String ip) throws CardDomainException{
        Page<Reason> responses = reasonService.getAllReasons(map, caller, ip);
        log.debug("Reasons retrieval completed by QueryHandler - Count: {}",
                responses != null ? responses.getContent().size() : 0);
        return reasonDataMapper.reasonsToGetResponses(responses);
    }

    @Transactional(readOnly = true)
    public ReasonListResponse findByGroup(ReasonGroup groupId) throws CardDomainException {
        log.info("Finding reasons by group: {}", groupId);
        java.util.List<Reason> reasons = reasonService.findByGroupId(groupId);
        log.debug("Reasons retrieval by group completed - groupId: {}, count: {}", groupId, reasons.size());
        return reasonDataMapper.reasonListToGetResponses(reasons);
    }
}

