package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.requests.ReissuanceCardRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for DuplicateCardIssuanceRequestEntity (صدور کارت المثنى)
 */
@Repository
public interface ReissuanceCardRequestJpaRepository extends JpaRepository<ReissuanceCardRequestEntity, Long> {
}
