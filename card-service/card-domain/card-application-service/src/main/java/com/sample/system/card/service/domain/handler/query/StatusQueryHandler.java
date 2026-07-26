package com.sample.system.card.service.domain.handler.query;

import com.sample.system.card.service.domain.entity.Status;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.StatusDataMapper;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.response.StatusListResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class StatusQueryHandler {

    private final StatusDataMapper statusDataMapper;
    private final StatusService statusService;

    public StatusListResponse listStatuses(Map<String, String> map, String caller, String ip) throws CardDomainException {
        Page<Status> responses = statusService.listStatuses(map, caller, ip);
        log.debug("Statuses retrieval completed by QueryHandler - Count: {}",
                responses != null ? responses.getContent().size() : 0);
        return statusDataMapper.statusesToGetListResponse(responses);
    }
}

