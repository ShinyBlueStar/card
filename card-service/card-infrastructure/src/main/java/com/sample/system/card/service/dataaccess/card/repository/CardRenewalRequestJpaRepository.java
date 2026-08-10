package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.requests.RenewalCardRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for CardRenewalRequestEntity (تمدید کارت)
 */
@Repository
public interface CardRenewalRequestJpaRepository extends JpaRepository<RenewalCardRequestEntity, Long> {
}
