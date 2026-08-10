package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.requests.ReplacementCardRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for IssueCardRequestEntity (صدور کارت منطقی)
 */
@Repository
public interface ReplacementCardRequestJpaRepository extends JpaRepository<ReplacementCardRequestEntity, Long> {
}
