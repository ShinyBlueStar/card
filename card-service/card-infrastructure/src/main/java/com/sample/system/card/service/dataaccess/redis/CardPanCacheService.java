package com.sample.system.card.service.dataaccess.redis;

import com.sample.system.card.service.domain.ports.output.repository.CardPanCacheRepository;
import com.sample.system.card.service.domain.utility.PanMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardPanCacheService implements CardPanCacheRepository {

    private final RedissonClient redissonClient;
    private static final String REDIS_HASH_KEY = "card:pans"; // Redis Hash key for all PANs

    public boolean exists(String pan) {
        if (pan == null || pan.isBlank()) {
            return false;
        }
        try {
            RMap<String, String> panMap = redissonClient.getMap(REDIS_HASH_KEY);
            return panMap.containsKey(pan);
        } catch (Exception e) {
            log.error("Error checking PAN existence in Redis cache: {}", PanMaskingUtil.maskPan(pan), e);
            // If Redis fails, return false to allow the process to continue
            // Database unique constraint will catch duplicates if Redis is unavailable
            return false;
        }
    }

    public void addPan(String pan) {
        if (pan == null || pan.isBlank()) {
            log.warn("Attempted to add null or empty PAN to cache");
            return;
        }
        try {
            RMap<String, String> panMap = redissonClient.getMap(REDIS_HASH_KEY);
            // Use PAN as both key and value (or store timestamp as value)
            panMap.put(pan, String.valueOf(System.currentTimeMillis()));
            log.debug("PAN added to Redis cache: {}", PanMaskingUtil.maskPan(pan));
        } catch (Exception e) {
            log.error("Error adding PAN to Redis cache: {}", PanMaskingUtil.maskPan(pan), e);
            // Log error but don't throw exception - cache is for optimization
            // Database unique constraint is the source of truth
        }
    }

    public long getCacheSize() {
        try {
            RMap<String, String> panMap = redissonClient.getMap(REDIS_HASH_KEY);
            return panMap.size();
        } catch (Exception e) {
            log.error("Error getting cache size", e);
            return 0;
        }
    }

    public Set<String> getAllPans() {
        try {
            RMap<String, String> panMap = redissonClient.getMap(REDIS_HASH_KEY);
            return panMap.keySet();
        } catch (Exception e) {
            log.error("Error getting all PANs from cache", e);
            return Set.of();
        }
    }

    public void addPansBatch(Set<String> pans) {
        if (pans == null || pans.isEmpty()) {
            return;
        }
        try {
            RMap<String, String> panMap = redissonClient.getMap(REDIS_HASH_KEY);
            long timestamp = System.currentTimeMillis();
            pans.forEach(pan -> {
                if (pan != null && !pan.isBlank()) {
                    panMap.put(pan, String.valueOf(timestamp));
                }
            });
            log.info("Batch added {} PANs to Redis cache", pans.size());
        } catch (Exception e) {
            log.error("Error batch adding PANs to Redis cache", e);
        }
    }
}
