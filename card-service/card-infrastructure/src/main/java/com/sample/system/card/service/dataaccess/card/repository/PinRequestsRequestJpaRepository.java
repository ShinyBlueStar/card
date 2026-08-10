package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.requests.PinRequestsRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for PinRequestsRequestEntity (درخواست‌های رمز - کلاس عمومی)
 */
@Repository
public interface PinRequestsRequestJpaRepository extends JpaRepository<PinRequestsRequestEntity, Long> {
}
