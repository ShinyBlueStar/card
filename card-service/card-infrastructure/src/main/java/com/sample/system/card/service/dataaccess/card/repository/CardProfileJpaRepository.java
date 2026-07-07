package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.CardProfileCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Collection;

@Repository
public interface CardProfileJpaRepository extends JpaRepository<CardProfileCommandEntity, Long>, JpaSpecificationExecutor<CardProfileCommandEntity> {

    Collection<CardProfileCommandEntity> findByIssuingBankId(Long bankId);

    Collection<CardProfileCommandEntity> findByIsActiveTrue();

    boolean existsByProfileName(String profileName);
}
