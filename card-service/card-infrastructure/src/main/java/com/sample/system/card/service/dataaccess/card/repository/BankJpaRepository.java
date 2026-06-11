package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.BankCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankJpaRepository extends JpaRepository<BankCommandEntity, Long>, JpaSpecificationExecutor<BankCommandEntity> {

    Optional<BankCommandEntity> findByBinCode(String binCode);

    List<BankCommandEntity> findByIsActiveTrue();

    boolean existsByBinCode(String binCode);

    boolean existsByName(String name);
}