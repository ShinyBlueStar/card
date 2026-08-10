package com.sample.system.card.service.domain.handler.query;

import com.sample.system.card.service.domain.command.cardRequest.GetAllCardRequestsQuery;
import com.sample.system.card.service.domain.command.cardRequest.GetCardRequestQuery;
import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardRequestDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardRequestService;
import com.sample.system.card.service.domain.response.request.GetCardRequestResponse;
import com.sample.system.card.service.domain.response.request.CardRequestListResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class CardRequestQueryHandler {

    private final CardRequestDataMapper cardRequestDataMapper;
    private final CardRequestService cardRequestService;

    public GetCardRequestResponse getCardRequest(GetCardRequestQuery query) throws CardDomainException {
        log.info("Getting card request with id: {}", query.getCardRequestId());
        CardRequest request = cardRequestDataMapper.queryToCardRequest(query);
        var cardRequest = cardRequestService.getCardRequest(request);
        log.info("CardRequest retrieval completed by QueryHandler - RequestId: {}", query.getCardRequestId());
        return cardRequestDataMapper.requestToGetResponse(cardRequest);
    }

    public List<GetCardRequestResponse> getAllCardRequests(GetAllCardRequestsQuery query) throws CardDomainException {
        log.info("Getting all card requests with status: {}, customerNumber: {}, cardProfileId: {}",
                query.getStatus(), query.getCustomerNumber(), query.getCardProfileId());
        var cardRequests = cardRequestService.getAllCardRequests(query);
        log.info("Found {} card requests", cardRequests.size());
        return cardRequestDataMapper.requestsToGetResponses(cardRequests);
    }

    @Transactional(readOnly = true)
    public CardRequestListResponse listCardRequests(Map<String, String> map, String caller, String ip) throws CardDomainException {
        Page<CardRequest> cardRequestsPage = cardRequestService.listCardRequests(map, caller, ip);
        log.debug("CardRequests retrieval completed by QueryHandler - Count: {}",
                cardRequestsPage != null ? cardRequestsPage.getContent().size() : 0);
        return cardRequestDataMapper.requestsToGetListResponse(cardRequestsPage);
    }
}
