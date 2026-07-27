package com.sample.system.card.service.domain.handler.query;

import com.sample.system.card.service.domain.command.card.GetAllCardsQuery;
import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardService;
import com.sample.system.card.service.domain.response.accessfile.AccessFileResponse;
import com.sample.system.card.service.domain.response.card.CardListResponse;
import com.sample.system.card.service.domain.response.card.GetCardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardQueryHandler {

    private final CardService cardService;
    private final CardDataMapper cardDataMapper;

    public GetCardResponse getCard(Long id) throws CardDomainException {
        log.info("Getting card with id: {}", id);

        Card card = cardService.getCard(id);
        log.info("Card retrieval completed by QueryHandler - CardId: {}", id);
        return cardDataMapper.cardToGetCardResponse(card);
    }

    public List<GetCardResponse> getAllCards(GetAllCardsQuery query) throws CardDomainException {
        log.info("Getting all cards with activeOnly: {}, customerNumber: {}",
                query.getActiveOnly(), query.getCustomerNumber());
        List<Card> cards = cardService.getAllCards(query);
        log.info("Found {} cards", cards.size());
        return cardDataMapper.cardsToGetResponses(cards);
    }

    public CardListResponse listCards(Map<String, String> map, String caller, String ip) throws CardDomainException {
        Page<Card> cardsPage = cardService.listCards(map, caller, ip);
        log.debug("Cards retrieval completed by QueryHandler - Count: {}",
                cardsPage != null ? cardsPage.getContent().size() : 0);
        return cardDataMapper.cardsToGetListResponse(cardsPage);
    }

    public AccessFileResponse generateAccessFile() throws CardDomainException {
        log.info("Generating Access export through QueryHandler");
        return cardService.generateAccessFile();
    }
}
