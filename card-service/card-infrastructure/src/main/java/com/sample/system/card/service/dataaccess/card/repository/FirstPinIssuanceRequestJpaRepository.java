package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.requests.FirstPinIssuanceRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for FirstPinIssuanceRequestEntity (صدور رمز اول)
 */
@Repository
public interface FirstPinIssuanceRequestJpaRepository extends JpaRepository<FirstPinIssuanceRequestEntity, Long> {
}
