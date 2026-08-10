package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.requests.ChangePinRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for ChangePinRequestEntity (درخواست تغییر رمز)
 */
@Repository
public interface ChangePinRequestJpaRepository extends JpaRepository<ChangePinRequestEntity, Long> {
}
