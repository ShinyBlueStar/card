package com.sample.system.card.service.domain.ports.output.repository;

public interface CardPanCacheRepository {

    boolean exists(String pan);

    void addPan(String pan);

    long getCacheSize();

    java.util.Set<String> getAllPans();

    void addPansBatch(java.util.Set<String> pans);
}
