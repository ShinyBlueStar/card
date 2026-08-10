package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.requests.FixedSecondPinIssuanceRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for FixedSecondPinIssuanceRequestEntity (صدور رمز دوم ثابت)
 */
@Repository
public interface FixedSecondPinIssuanceRequestJpaRepository extends JpaRepository<FixedSecondPinIssuanceRequestEntity, Long> {
}
