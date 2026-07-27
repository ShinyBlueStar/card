package com.sample.system.card.service.domain.scheduled;

import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.command.card.GetAllCardsQuery;
import com.sample.system.card.service.domain.ports.input.service.CardService;
import com.sample.system.card.service.domain.ports.output.repository.CardPanCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(CardPanCacheRepository.class)
public class CardPanCacheSyncJob {

    private final CardService cardService;
    private final CardPanCacheRepository cardPanCacheRepository;

    /**
     * Scheduled job هر ۱۰ دقیقه برای sync شماره‌های کارت از دیتابیس به Redis
     * This job ensures that all PANs in database are also present in Redis cache
     */
    public void syncWithDatabase() {
        log.info("Starting PAN Redis sync job...");
        try {
            // Get all cards from database
            List<Card> allCards = cardService.getAllCards(new GetAllCardsQuery(null, null));

            // Extract PANs from cards
            Set<String> databasePans = allCards.stream()
                    .map(Card::getPan)
                    .filter(pan -> pan != null && !pan.isBlank())
                    .collect(Collectors.toSet());

            log.info("Found {} PANs in database", databasePans.size());

            // Get existing PANs from Redis cache
            Set<String> cachedPans = cardPanCacheRepository.getAllPans();
            log.info("Found {} PANs in Redis cache", cachedPans.size());

            // Find PANs in database that are not in cache
            Set<String> pansToAdd = databasePans.stream()
                    .filter(pan -> !cachedPans.contains(pan))
                    .collect(Collectors.toSet());

            if (!pansToAdd.isEmpty()) {
                log.info("Adding {} new PANs to Redis cache", pansToAdd.size());
                cardPanCacheRepository.addPansBatch(pansToAdd);
            } else {
                log.info("No new PANs to add to Redis cache - cache is in sync");
            }

            long finalCacheSize = cardPanCacheRepository.getCacheSize();
            log.info("PAN Redis sync job completed. Total entries in cache: {}", finalCacheSize);

        } catch (Exception e) {
            log.error("Error occurred during PAN Redis sync job", e);
            // Don't throw exception - sync job should not break the application
        }
    }
}
